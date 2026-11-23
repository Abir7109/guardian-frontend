import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../../core/painters/radar_painter.dart';
import '../../theme/colors.dart';
import '../../theme/guardian_theme.dart';
import '../../theme/tokens.dart';

/// Result of a PIN verification attempt.
enum PinResult { ok, wrong, lockout }

/// Contract the screen depends on — implemented by `PinService` (secure storage,
/// salted hash, attempt counter). The Kotlin app hardcoded `pinCode == "1234"`;
/// do not carry that forward.
abstract interface class PinVerifier {
  Future<PinResult> verify(String pin);
  Future<bool> biometricUnlock();
  int get remainingAttempts;
}

/// Full-screen lockdown protocol.
///
/// Kotlin reference `AlarmOverlayScreen` (L2329-2629):
///   breach banner 32 dp + live HH:mm:ss
///   heartbeat radial alpha .05 -> .20 over 1500 ms easeInOutSine, reverse
///   160 dp orbit, 80 dp shield with coloured shadow
///   'DEVICE LOCKED' 32/800/ls2
///   4 dots 14 dp, gap 20
///   288 dp keypad, 4x3, aspect 1
///   spring shake on wrong PIN (3 sequential springs: +20, -20, 0)
///
/// Deviations: 6 digits instead of 4, real verification, attempt lockout,
/// error state visible for 1.2 s (Kotlin cleared it in ~300 ms), POP BLOCKED.
class LockdownScreen extends StatefulWidget {
  const LockdownScreen({
    super.key,
    required this.verifier,
    required this.onUnlocked,
    required this.onCancel,
    this.pinLength = 6,
    this.reason = 'SECURITY BREACH LOCKDOWN ACTIVE',
  });

  final PinVerifier verifier;
  final VoidCallback onUnlocked;
  final VoidCallback onCancel;
  final int pinLength;
  final String reason;

  @override
  State<LockdownScreen> createState() => _LockdownScreenState();
}

class _LockdownScreenState extends State<LockdownScreen>
    with TickerProviderStateMixin {
  String _pin = '';
  bool _error = false;
  bool _success = false;
  bool _busy = false;
  int _lockoutSeconds = 0;
  Timer? _clock;
  Timer? _lockoutTimer;
  late String _time;

  late final AnimationController _heartbeat;
  late final AnimationController _shake;
  late final Animation<double> _shakeDx;

  @override
  void initState() {
    super.initState();
    _time = _format(DateTime.now());
    _clock = Timer.periodic(const Duration(seconds: 1), (_) {
      if (mounted) setState(() => _time = _format(DateTime.now()));
    });

    // Heartbeat: alpha .05 -> .20, 1500 ms, easeInOutSine, reverse.
    _heartbeat = AnimationController(
      vsync: this,
      duration: GuardianMotion.heartbeat,
    )..repeat(reverse: true);

    // Shake: +20 -> -20 -> 0 (Kotlin used 3 sequential high-bouncy springs).
    _shake = AnimationController(
      vsync: this,
      duration: GuardianMotion.shake,
    );
    _shakeDx = TweenSequence<double>([
      TweenSequenceItem(
        tween: Tween(begin: 0.0, end: 20.0).chain(CurveTween(curve: Curves.elasticOut)),
        weight: 35,
      ),
      TweenSequenceItem(
        tween: Tween(begin: 20.0, end: -20.0).chain(CurveTween(curve: Curves.elasticOut)),
        weight: 35,
      ),
      TweenSequenceItem(
        tween: Tween(begin: -20.0, end: 0.0).chain(CurveTween(curve: Curves.easeOut)),
        weight: 30,
      ),
    ]).animate(_shake);
  }

  static String _format(DateTime d) =>
      '${d.hour.toString().padLeft(2, '0')}:'
      '${d.minute.toString().padLeft(2, '0')}:'
      '${d.second.toString().padLeft(2, '0')}';

  @override
  void dispose() {
    _clock?.cancel();
    _lockoutTimer?.cancel();
    _heartbeat.dispose();
    _shake.dispose();
    super.dispose();
  }

  void _onKey(String key) {
    if (_busy || _lockoutSeconds > 0) return;
    HapticFeedback.selectionClick();
    setState(() {
      if (key == 'DEL') {
        if (_pin.isNotEmpty) _pin = _pin.substring(0, _pin.length - 1);
      } else if (key == 'CANCEL') {
        widget.onCancel();
      } else if (_pin.length < widget.pinLength) {
        _pin += key;
      }
      if (_error) _error = false;
    });
    if (_pin.length == widget.pinLength) _verify();
  }

  Future<void> _verify() async {
    setState(() => _busy = true);
    final result = await widget.verifier.verify(_pin);
    if (!mounted) return;

    switch (result) {
      case PinResult.ok:
        setState(() {
          _success = true;
          _busy = false;
        });
        HapticFeedback.heavyImpact();
        await Future<void>.delayed(const Duration(milliseconds: 600));
        if (mounted) widget.onUnlocked();
      case PinResult.wrong:
        HapticFeedback.vibrate();
        setState(() {
          _error = true;
          _busy = false;
          _pin = '';
        });
        _shake.forward(from: 0);
        // Keep the error colour visible long enough to register.
        await Future<void>.delayed(const Duration(milliseconds: 1200));
        if (mounted) setState(() => _error = false);
      case PinResult.lockout:
        setState(() {
          _busy = false;
          _pin = '';
          _lockoutSeconds = 30;
        });
        _startLockoutTimer();
    }
  }

  void _startLockoutTimer() {
    _lockoutTimer?.cancel();
    _lockoutTimer = Timer.periodic(const Duration(seconds: 1), (t) {
      if (!mounted) return t.cancel();
      setState(() => _lockoutSeconds--);
      if (_lockoutSeconds <= 0) t.cancel();
    });
  }

  @override
  Widget build(BuildContext context) {
    // The lockdown screen must not be dismissible.
    return PopScope(
      canPop: false,
      child: Scaffold(
        backgroundColor: GuardianColors.voidBlack,
        body: Stack(
          children: [
            // Heartbeat plasma glow
            AnimatedBuilder(
              animation: _heartbeat,
              builder: (context, _) {
                final a = 0.05 + 0.15 * Curves.easeInOutSine.transform(_heartbeat.value);
                return DecoratedBox(
                  decoration: BoxDecoration(
                    gradient: RadialGradient(
                      radius: 0.9,
                      colors: [
                        (_success ? GuardianColors.cyan : GuardianColors.alert)
                            .withValues(alpha: a),
                        Colors.transparent,
                      ],
                    ),
                  ),
                  child: const SizedBox.expand(),
                );
              },
            ),
            SafeArea(
              child: Column(
                children: [
                  _BreachBanner(time: _time, message: widget.reason),
                  Expanded(
                    child: AnimatedBuilder(
                      animation: _shakeDx,
                      builder: (context, child) => Transform.translate(
                        offset: Offset(_shakeDx.value, 0),
                        child: child,
                      ),
                      child: _content(context),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _content(BuildContext context) {
    final accent = _success
        ? GuardianColors.cyan
        : _error
            ? GuardianColors.alertBright
            : GuardianColors.alert;
    final pr = MediaQuery.devicePixelRatioOf(context);

    return SingleChildScrollView(
      padding: const EdgeInsets.symmetric(horizontal: GuardianSpacing.x4),
      child: Column(
        children: [
          const SizedBox(height: GuardianSpacing.x5),
          SizedBox(
            width: 160,
            height: 160,
            child: Stack(
              alignment: Alignment.center,
              children: [
                CustomPaint(
                  size: const Size.square(160),
                  painter: LockOrbitPainter(pixelRatio: pr),
                ),
                Container(
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    boxShadow: guardianGlow(accent, blur: 30, alpha: 0.35),
                  ),
                  child: Icon(Icons.shield, size: 80, color: accent),
                ),
              ],
            ),
          ),
          const SizedBox(height: GuardianSpacing.x4),
          Text(
            _success ? 'GRID ACCESSIBLE' : 'DEVICE LOCKED',
            textAlign: TextAlign.center,
            style: GuardianType.displayLg.copyWith(color: accent),
          ),
          const SizedBox(height: GuardianSpacing.xxl),
          _PinDots(
            length: widget.pinLength,
            filled: _pin.length,
            error: _error,
            success: _success,
          ),
          if (_lockoutSeconds > 0) ...[
            const SizedBox(height: GuardianSpacing.md),
            Text(
              'TOO MANY ATTEMPTS — RETRY IN ${_lockoutSeconds}s',
              style: GuardianType.labelMicro.copyWith(color: GuardianColors.alert),
            ),
          ],
          const SizedBox(height: GuardianSpacing.x5),
          _Keypad(onKey: _onKey, enabled: !_busy && _lockoutSeconds == 0),
          const SizedBox(height: GuardianSpacing.xl),
          TextButton.icon(
            onPressed: _busy
                ? null
                : () async {
                    final ok = await widget.verifier.biometricUnlock();
                    if (ok && mounted) widget.onUnlocked();
                  },
            icon: const Icon(Icons.fingerprint, size: 18),
            label: Text(
              'USE BIOMETRICS',
              style: GuardianType.label.copyWith(fontSize: 11),
            ),
            style: TextButton.styleFrom(
              foregroundColor: GuardianColors.periwinkle,
            ),
          ),
          const SizedBox(height: GuardianSpacing.x5),
        ],
      ),
    );
  }
}

class _BreachBanner extends StatelessWidget {
  const _BreachBanner({required this.time, required this.message});

  final String time;
  final String message;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 32,
      padding: const EdgeInsets.symmetric(horizontal: GuardianSpacing.xxl),
      decoration: BoxDecoration(
        color: GuardianColors.alert.withValues(alpha: 0.2),
        border: Border.all(
          color: GuardianColors.alert.withValues(alpha: 0.5),
          width: GuardianRadius.hairline,
        ),
      ),
      child: Row(
        children: [
          Expanded(
            child: Text(
              '!!! ${message.toUpperCase()} !!!',
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: GuardianType.labelMicro.copyWith(
                color: GuardianColors.alert,
                fontSize: 10,
              ),
            ),
          ),
          Text(
            time,
            style: GuardianType.labelMicro.copyWith(
              color: GuardianColors.alert,
              fontSize: 11,
              fontFeatures: const [FontFeature.tabularFigures()],
            ),
          ),
        ],
      ),
    );
  }
}

class _PinDots extends StatelessWidget {
  const _PinDots({
    required this.length,
    required this.filled,
    required this.error,
    required this.success,
  });

  final int length;
  final int filled;
  final bool error;
  final bool success;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      liveRegion: true,
      label: 'PIN entry, $filled of $length digits',
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          for (var i = 0; i < length; i++) ...[
            if (i > 0) const SizedBox(width: GuardianSpacing.x3),
            AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              width: 14,
              height: 14,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: success
                    ? GuardianColors.cyan
                    : error
                        ? GuardianColors.alertBright
                        : i < filled
                            ? GuardianColors.periwinkle
                            : GuardianColors.outlineVariant.withValues(alpha: 0.6),
                border: Border.all(
                  color: i < filled
                      ? GuardianColors.periwinkle
                      : GuardianColors.outlineVariant.withValues(alpha: 0.4),
                  width: 1,
                ),
                boxShadow: (i < filled || success)
                    ? guardianGlow(
                        error ? GuardianColors.alert : GuardianColors.periwinkle,
                        blur: 8,
                        alpha: 0.7,
                      )
                    : null,
              ),
            ),
          ],
        ],
      ),
    );
  }
}

class _Keypad extends StatelessWidget {
  const _Keypad({required this.onKey, required this.enabled});

  final ValueChanged<String> onKey;
  final bool enabled;

  static const _rows = [
    ['1', '2', '3'],
    ['4', '5', '6'],
    ['7', '8', '9'],
    ['CANCEL', '0', 'DEL'],
  ];

  static const _subLabels = {
    '2': 'ABC',
    '3': 'DEF',
    '4': 'GHI',
    '5': 'JKL',
    '6': 'MNO',
    '7': 'PQRS',
    '8': 'TUV',
    '9': 'WXYZ',
  };

  @override
  Widget build(BuildContext context) {
    return Opacity(
      opacity: enabled ? 1 : 0.5,
      child: SizedBox(
        width: GuardianSizes.pinKeypad,
        child: Column(
          children: [
            for (final row in _rows)
              Padding(
                padding: const EdgeInsets.only(bottom: GuardianSpacing.xl),
                child: Row(
                  children: [
                    for (var i = 0; i < row.length; i++) ...[
                      if (i > 0) const SizedBox(width: GuardianSpacing.xxl),
                      Expanded(
                        child: _Key(
                          value: row[i],
                          sub: _subLabels[row[i]],
                          onTap: enabled ? () => onKey(row[i]) : null,
                        ),
                      ),
                    ],
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _Key extends StatelessWidget {
  const _Key({required this.value, required this.onTap, this.sub});

  final String value;
  final String? sub;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final isDel = value == 'DEL';
    final isCancel = value == 'CANCEL';

    final semantic = isDel
        ? 'Delete last digit'
        : isCancel
            ? 'Cancel lockdown'
            : 'Digit $value';

    return Semantics(
      button: true,
      label: semantic,
      child: AspectRatio(
        aspectRatio: 1,
        child: Material(
          color: GuardianColors.slate.withValues(alpha: 0.4),
          shape: CircleBorder(
            side: BorderSide(
              color: GuardianColors.periwinkle.withValues(alpha: 0.1),
              width: GuardianRadius.hairline,
            ),
          ),
          clipBehavior: Clip.antiAlias,
          child: InkWell(
            onTap: onTap,
            customBorder: const CircleBorder(),
            child: Center(
              child: isDel
                  ? const Icon(
                      Icons.backspace_outlined,
                      size: 20,
                      color: GuardianColors.alert,
                    )
                  : isCancel
                      ? Text(
                          'CANCEL',
                          style: GuardianType.labelNano.copyWith(
                            fontSize: 10,
                            color: GuardianColors.outline,
                          ),
                        )
                      : Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              value,
                              style: GuardianType.mono.copyWith(
                                fontSize: 24,
                                fontWeight: FontWeight.w600,
                                color: GuardianColors.onSurface,
                              ),
                            ),
                            if (sub != null)
                              Text(
                                sub!,
                                style: GuardianType.labelNano.copyWith(
                                  fontSize: 8,
                                  color: GuardianColors.outline.withValues(alpha: 0.6),
                                ),
                              ),
                          ],
                        ),
            ),
          ),
        ),
      ),
    );
  }
}

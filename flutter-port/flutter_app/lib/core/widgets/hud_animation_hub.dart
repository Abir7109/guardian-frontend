import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../theme/colors.dart';
import '../../theme/tokens.dart';
import '../painters/radar_painter.dart';
import '../painters/safe_zone_painter.dart';

/// Consolidates the Kotlin app's six independent `rememberInfiniteTransition`s
/// into ONE AnimationController with staggered phases.
///
/// Why: each `rememberInfiniteTransition` in Compose is effectively its own
/// driver; six of them rasterise independently. A single controller rebuilding
/// one `AnimatedBuilder` keeps the frame budget flat, which is what lets this
/// screen hold 60 fps on mid-range Android.
///
/// Exposes: radarOuter, radarInner, glow, pulse.
class HudAnimationHub extends StatefulWidget {
  const HudAnimationHub({
    super.key,
    required this.builder,
    this.animate = true,
  });

  final Widget Function(
    BuildContext context,
    HudAnimValues values,
  ) builder;

  final bool animate;

  @override
  State<HudAnimationHub> createState() => _HudAnimationHubState();
}

class HudAnimValues {
  const HudAnimValues({
    required this.radarOuter,
    required this.radarInner,
    required this.glow,
    required this.pulse,
  });

  final double radarOuter; // 0..1
  final double radarInner; // 0..1
  final double glow; // 0.95..1.05 scale
  final double pulse; // 0..1

  double get radarOuterRad => radarOuter * 2 * math.pi;
  double get radarInnerRad => (1 - radarInner) * 2 * math.pi;
}

class _HudAnimationHubState extends State<HudAnimationHub>
    with TickerProviderStateMixin {
  late final AnimationController _outer;
  late final AnimationController _inner;
  late final AnimationController _breathe;
  late final AnimationController _pulse;
  bool _reduceMotion = false;

  @override
  void initState() {
    super.initState();
    _outer = AnimationController(vsync: this, duration: GuardianMotion.radarOuter);
    _inner = AnimationController(vsync: this, duration: GuardianMotion.radarInner);
    _breathe = AnimationController(vsync: this, duration: GuardianMotion.breathe);
    _pulse = AnimationController(vsync: this, duration: GuardianMotion.breathe);
    if (widget.animate) _start();
  }

  void _start() {
    _outer.repeat();
    _inner.repeat();
    _breathe.repeat(reverse: true);
    _pulse.repeat();
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    // Respect the OS "remove animations" accessibility setting.
    final reduce = MediaQuery.maybeDisableAnimationsOf(context) ?? false;
    if (reduce != _reduceMotion) {
      _reduceMotion = reduce;
      if (reduce) {
        for (final c in [_outer, _inner, _breathe, _pulse]) {
          c.stop();
        }
      } else if (widget.animate) {
        _start();
      }
    }
  }

  @override
  void dispose() {
    _outer.dispose();
    _inner.dispose();
    _breathe.dispose();
    _pulse.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: Listenable.merge([_outer, _inner, _breathe, _pulse]),
      builder: (context, _) => widget.builder(
        context,
        HudAnimValues(
          radarOuter: _outer.value,
          radarInner: _inner.value,
          glow: 0.95 + 0.1 * Curves.easeInOutSine.transform(_breathe.value),
          pulse: _pulse.value,
        ),
      ),
    );
  }
}

/// Pre-composed radar + core-button stack, ready to drop into Home.
class SystemCore extends StatelessWidget {
  const SystemCore({
    super.key,
    required this.label,
    required this.onTap,
    this.active = true,
    this.size = GuardianSizes.radar,
  });

  final String label;
  final VoidCallback onTap;
  final bool active;
  final double size;

  @override
  Widget build(BuildContext context) {
    final pr = MediaQuery.devicePixelRatioOf(context);
    final accent = active ? GuardianColors.cyan : GuardianColors.outline;

    return HudAnimationHub(
      builder: (context, a) => SizedBox(
        width: size,
        height: size,
        child: Stack(
          alignment: Alignment.center,
          children: [
            Transform.scale(
              scale: a.glow,
              child: CustomPaint(
                size: Size.square(size),
                painter: RadarPainter(
                  outerAngle: a.radarOuterRad,
                  innerAngle: a.radarInnerRad,
                  pixelRatio: pr,
                  activeColor: accent,
                  inactiveColor: active
                      ? GuardianColors.periwinkle
                      : GuardianColors.outlineVariant,
                ),
              ),
            ),
            Transform.scale(
              scale: a.glow,
              child: Semantics(
                button: true,
                label: label,
                child: Material(
                  color: GuardianColors.surfaceLowest,
                  shape: const CircleBorder(
                    side: BorderSide(color: Color(0x8049FCD9)),
                  ),
                  child: InkWell(
                    customBorder: const CircleBorder(),
                    onTap: onTap,
                    child: SizedBox(
                      width: GuardianSizes.core,
                      height: GuardianSizes.core,
                      child: DecoratedBox(
                        decoration: BoxDecoration(
                          shape: BoxShape.circle,
                          boxShadow: guardianGlow(accent, blur: 24, alpha: 0.35),
                        ),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Icon(Icons.shield, size: 36, color: accent),
                            const SizedBox(height: GuardianSpacing.md),
                            Text(
                              label.toUpperCase(),
                              style: TextStyle(
                                fontSize: 11,
                                fontWeight: FontWeight.w700,
                                letterSpacing: 1,
                                color: accent,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// Safe Zone map surface with the code-drawn HUD overlay.
class SafeZoneMap extends StatelessWidget {
  const SafeZoneMap({super.key, this.height = 160});

  final double height;

  @override
  Widget build(BuildContext context) {
    final pr = MediaQuery.devicePixelRatioOf(context);
    return ClipRRect(
      borderRadius: BorderRadius.circular(GuardianRadius.tile),
      child: Container(
        height: height,
        decoration: BoxDecoration(
          color: GuardianColors.surfaceLowest,
          border: Border.all(
            color: GuardianColors.outlineVariant.withValues(alpha: 0.5),
            width: GuardianRadius.hairline,
          ),
          borderRadius: BorderRadius.circular(GuardianRadius.tile),
        ),
        child: Stack(
          fit: StackFit.expand,
          children: [
            // Bundled asset replaces the unstable lh3.googleusercontent.com URL
            // used by the Kotlin app (which also had no INTERNET permission).
            Image.asset(
              'assets/illustrations/safe_zone_map.png',
              fit: BoxFit.cover,
              opacity: const AlwaysStoppedAnimation(0.9),
              errorBuilder: (_, __, ___) => const SizedBox.shrink(),
            ),
            HudAnimationHub(
              builder: (context, a) => CustomPaint(
                painter: SafeZoneMapPainter(
                  pixelRatio: pr,
                  pulseProgress: a.pulse,
                ),
              ),
            ),
            const Center(
              child: Padding(
                padding: EdgeInsets.only(bottom: 12),
                child: Icon(
                  Icons.location_on,
                  size: 24,
                  color: GuardianColors.cyan,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

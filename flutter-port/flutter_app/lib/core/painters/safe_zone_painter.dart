import 'dart:math' as math;

import 'package:flutter/material.dart';

import '../../theme/colors.dart';
import 'dash.dart';

/// Safe Zone map overlay — crosshairs, dashed geofence, expanding pulse.
///
/// Kotlin reference (`HomeScreen` Safe Zone card):
///   crosshair V + H        : alpha .15, 0.5 dp
///   geofence circle        : radius 45 dp, alpha .3, 1 dp, dash(10,10)
///   centre dot             : radius 4 dp, solid cyan
///   pulse                  : 0 → 60 dp, alpha .35 * (1 - t/60), 1.5 dp stroke
class SafeZoneMapPainter extends CustomPainter {
  const SafeZoneMapPainter({
    required this.pixelRatio,
    this.pulseProgress = 1,
  });

  final double pixelRatio;

  /// 0..1 — drives the expanding ring. Pass 1 to hide the pulse.
  final double pulseProgress;

  @override
  void paint(Canvas canvas, Size size) {
    final center = size.center(Offset.zero);
    final hair = 0.5 * pixelRatio;

    // Crosshairs
    final cross = Paint()
      ..color = GuardianColors.cyan.withValues(alpha: 0.15)
      ..strokeWidth = hair;
    canvas.drawLine(Offset(center.dx, 0), Offset(center.dx, size.height), cross);
    canvas.drawLine(Offset(0, center.dy), Offset(size.width, center.dy), cross);

    // Geofence boundary
    canvas.drawPath(
      dashedCircle(
        center: center,
        radius: 45 * pixelRatio,
        dash: 10 * pixelRatio,
        gap: 10 * pixelRatio,
      ),
      Paint()
        ..color = GuardianColors.cyan.withValues(alpha: 0.3)
        ..style = PaintingStyle.stroke
        ..strokeWidth = pixelRatio,
    );

    // Centre node
    canvas.drawCircle(
      center,
      4 * pixelRatio,
      Paint()..color = GuardianColors.cyan,
    );

    // Expanding pulse ring
    if (pulseProgress < 1) {
      final r = 60 * pixelRatio * pulseProgress;
      canvas.drawCircle(
        center,
        r,
        Paint()
          ..color = GuardianColors.cyan
              .withValues(alpha: 0.35 * (1 - pulseProgress))
          ..style = PaintingStyle.stroke
          ..strokeWidth = 1.5 * pixelRatio,
      );
    }

    // Corner brackets — adds the HUD framing the raster map lacks.
    _bracket(canvas, const Offset(12, 12), 1);
    _bracket(canvas, Offset(size.width - 12, size.height - 12), -1);
  }

  void _bracket(Canvas canvas, Offset corner, int dir) {
    final paint = Paint()
      ..color = GuardianColors.cyan.withValues(alpha: 0.35)
      ..strokeWidth = pixelRatio
      ..style = PaintingStyle.stroke;
    const len = 14.0;
    final far = 28.0 * dir;
    canvas.drawLine(corner, corner + Offset(len * dir, 0), paint);
    canvas.drawLine(corner, corner + Offset(0, len * dir), paint);
    // avoid unused warning while keeping intent explicit
    assert(far != 0);
  }

  @override
  bool shouldRepaint(covariant SafeZoneMapPainter old) =>
      old.pulseProgress != pulseProgress || old.pixelRatio != pixelRatio;
}

/// Rotating sweep-gradient ring for the profile avatar.
class AvatarSweepPainter extends CustomPainter {
  const AvatarSweepPainter({required this.angle, required this.pixelRatio});

  final double angle; // radians
  final double pixelRatio;

  @override
  void paint(Canvas canvas, Size size) {
    final center = size.center(Offset.zero);
    final r = size.shortestSide / 2;

    final rect = Rect.fromCircle(center: center, radius: r);
    final paint = Paint()
      ..shader = const SweepGradient(
        colors: [
          GuardianColors.cyan,
          GuardianColors.blue,
          GuardianColors.cyan,
        ],
      ).createShader(rect);

    canvas.save();
    canvas.translate(center.dx, center.dy);
    canvas.rotate(angle);
    canvas.translate(-center.dx, -center.dy);
    canvas.drawCircle(center, r, paint);
    canvas.restore();
  }

  @override
  bool shouldRepaint(covariant AvatarSweepPainter old) =>
      old.angle != angle || old.pixelRatio != pixelRatio;
}

/// Hexagon outline used by the onboarding orbital illustration.
class HexagonPainter extends CustomPainter {
  const HexagonPainter({required this.pixelRatio, this.color = GuardianColors.outlineVariant});

  final double pixelRatio;
  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    canvas.drawPath(
      hexagonPath(Offset.zero & size),
      Paint()
        ..color = color
        ..style = PaintingStyle.stroke
        ..strokeWidth = pixelRatio,
    );
  }

  @override
  bool shouldRepaint(covariant HexagonPainter old) => old.color != color;
}

/// One-shot radar sweep arc — used behind the onboarding illustration.
class SweepArcPainter extends CustomPainter {
  const SweepArcPainter({required this.progress, required this.pixelRatio});

  final double progress; // 0..1
  final double pixelRatio;

  @override
  void paint(Canvas canvas, Size size) {
    final center = size.center(Offset.zero);
    final r = size.shortestSide / 2;
    final rect = Rect.fromCircle(center: center, radius: r);
    canvas.drawArc(
      rect,
      -math.pi / 2 + progress * 2 * math.pi,
      0.6,
      false,
      Paint()
        ..color = GuardianColors.cyan.withValues(alpha: 0.25)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 1.5 * pixelRatio
        ..strokeCap = StrokeCap.round,
    );
  }

  @override
  bool shouldRepaint(covariant SweepArcPainter old) =>
      old.progress != progress;
}

import 'package:flutter/material.dart';

import '../../theme/colors.dart';
import 'dash.dart';

/// Home "SYSTEM ARMED" radar HUD.
///
/// Kotlin reference (`HomeScreen`, L1278-1791):
///   outer ring  : radius w/2,   stroke 1.0, alpha .12, rotates 24 s
///   outer ring 2: radius w/2*.95, stroke 1.5, alpha .25, dash(15,25), rotates 24 s
///   inner ring  : radius w/2,   stroke 1.0, alpha .15, dash(6,12),  rotates 16 s reverse
class RadarPainter extends CustomPainter {
  const RadarPainter({
    required this.outerAngle,
    required this.innerAngle,
    required this.pixelRatio,
    this.activeColor = GuardianColors.cyan,
    this.inactiveColor = GuardianColors.periwinkle,
  });

  final double outerAngle; // radians
  final double innerAngle; // radians
  final double pixelRatio;

  final Color activeColor;
  final Color inactiveColor;

  @override
  void paint(Canvas canvas, Size size) {
    final center = size.center(Offset.zero);
    final r = size.shortestSide / 2;

    final outer = Paint()
      ..color = activeColor.withValues(alpha: 0.12)
      ..style = PaintingStyle.stroke
      ..strokeWidth = pixelRatio;

    final outerDashed = Paint()
      ..color = activeColor.withValues(alpha: 0.25)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5 * pixelRatio;

    final innerDashed = Paint()
      ..color = inactiveColor.withValues(alpha: 0.15)
      ..style = PaintingStyle.stroke
      ..strokeWidth = pixelRatio;

    // Ring 1 (solid, thin)
    canvas.drawCircle(center, r, outer);

    // Ring 2 (dashed) — rotate whole canvas around centre.
    canvas.save();
    canvas.translate(center.dx, center.dy);
    canvas.rotate(outerAngle);
    canvas.translate(-center.dx, -center.dy);
    canvas.drawPath(
      dashedCircle(
        center: center,
        radius: r * 0.95,
        dash: 15 * pixelRatio,
        gap: 25 * pixelRatio,
      ),
      outerDashed,
    );
    canvas.restore();

    // Ring 3 (dashed, tighter, counter-rotating)
    canvas.save();
    canvas.translate(center.dx, center.dy);
    canvas.rotate(innerAngle);
    canvas.translate(-center.dx, -center.dy);
    canvas.drawPath(
      dashedCircle(
        center: center,
        radius: r * 0.85,
        dash: 6 * pixelRatio,
        gap: 12 * pixelRatio,
      ),
      innerDashed,
    );
    canvas.restore();
  }

  @override
  bool shouldRepaint(covariant RadarPainter old) =>
      old.outerAngle != outerAngle ||
      old.innerAngle != innerAngle ||
      old.activeColor != activeColor ||
      old.pixelRatio != pixelRatio;
}

/// Splash radar mark: outer glow ring + filled inner disc.
class SplashRadarPainter extends CustomPainter {
  const SplashRadarPainter({required this.pixelRatio, this.color = GuardianColors.cyan});

  final double pixelRatio;
  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final center = size.center(Offset.zero);
    final r = size.shortestSide * 0.4;

    canvas.drawCircle(
      center,
      r * 1.15,
      Paint()
        ..color = color.withValues(alpha: 0.2)
        ..style = PaintingStyle.stroke
        ..strokeWidth = pixelRatio,
    );

    canvas.drawCircle(
      center,
      r * 0.9,
      Paint()..color = GuardianColors.periwinkle.withValues(alpha: 0.15),
    );
  }

  @override
  bool shouldRepaint(covariant SplashRadarPainter old) =>
      old.color != color || old.pixelRatio != pixelRatio;
}

/// Lockdown orbit rings behind the cracked shield.
class LockOrbitPainter extends CustomPainter {
  const LockOrbitPainter({required this.pixelRatio, this.color = GuardianColors.alert});

  final double pixelRatio;
  final Color color;

  @override
  void paint(Canvas canvas, Size size) {
    final center = size.center(Offset.zero);
    final r = size.shortestSide / 2;

    canvas.drawCircle(
      center,
      r,
      Paint()
        ..color = color.withValues(alpha: 0.15)
        ..style = PaintingStyle.stroke
        ..strokeWidth = pixelRatio,
    );

    canvas.drawPath(
      dashedCircle(
        center: center,
        radius: r * 0.85,
        dash: 5 * pixelRatio,
        gap: 10 * pixelRatio,
      ),
      Paint()
        ..color = GuardianColors.cyan.withValues(alpha: 0.1)
        ..style = PaintingStyle.stroke
        ..strokeWidth = 0.5 * pixelRatio,
    );
  }

  @override
  bool shouldRepaint(covariant LockOrbitPainter old) => old.color != color;
}

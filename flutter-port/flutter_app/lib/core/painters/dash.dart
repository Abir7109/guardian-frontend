import 'dart:ui';

/// Flutter's Paint has NO dash support. Compose's `PathEffect.dashPathEffect`
/// must be reproduced by extracting path metrics.
///
/// Kotlin used three interval pairs:
///   home radar outer ring : (15, 25)
///   home radar inner ring : (6, 12)
///   safe-zone geofence    : (10, 10)
///
/// IMPORTANT: intervals in the Kotlin source are dp and go through `toPx()`.
/// Painters have no BuildContext, so pass `pixelRatio` in and scale here,
/// otherwise dashes render half-size on high-density screens.
Path dashPath(
  Path source, {
  required double dash,
  required double gap,
  double phase = 0,
}) {
  final dest = Path();
  for (final metric in source.computeMetrics()) {
    var distance = phase % (dash + gap);
    // Emit a partial leading dash when a phase offset is supplied.
    if (distance > 0) {
      final leadEnd = (dash - distance).clamp(0.0, metric.length);
      if (leadEnd > 0) dest.addPath(metric.extractPath(0, leadEnd), Offset.zero);
      distance = dash + gap - distance;
    }
    while (distance < metric.length) {
      final next = (distance + dash).clamp(0.0, metric.length);
      dest.addPath(metric.extractPath(distance, next), Offset.zero);
      distance = next + gap;
    }
  }
  return dest;
}

/// Convenience: dashed circle as a ready-to-draw path.
Path dashedCircle({
  required Offset center,
  required double radius,
  required double dash,
  required double gap,
}) =>
    dashPath(
      Path()..addOval(Rect.fromCircle(center: center, radius: radius)),
      dash: dash,
      gap: gap,
    );

/// Regular polygon path — used for the onboarding hexagon.
/// Matches the Kotlin construction (6 vertices, 10 dp inset).
Path hexagonPath(Rect rect) {
  final cx = rect.center.dx;
  final left = rect.left + 10;
  final right = rect.right - 10;
  final top = rect.top + 10;
  final bottom = rect.bottom - 10;
  return Path()
    ..moveTo(cx, top)
    ..lineTo(right, rect.top + rect.height * 0.25)
    ..lineTo(right, rect.top + rect.height * 0.75)
    ..lineTo(cx, bottom)
    ..lineTo(left, rect.top + rect.height * 0.75)
    ..lineTo(left, rect.top + rect.height * 0.25)
    ..close();
}

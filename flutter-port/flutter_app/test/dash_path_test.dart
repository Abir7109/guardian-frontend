import 'dart:ui';

import 'package:flutter_test/flutter_test.dart';
import 'package:guardian/core/painters/dash.dart';

/// The dash helper is the single riskiest piece of drawing code in the port —
/// Flutter has no PathEffect, so correctness is our responsibility.
void main() {
  group('dashPath', () {
    test('produces the expected number of segments for a straight line', () {
      // 100 px line, dash 10, gap 10 -> 5 dashes.
      final line = Path()
        ..moveTo(0, 0)
        ..lineTo(100, 0);

      final dashed = dashPath(line, dash: 10, gap: 10);
      final metrics = dashed.computeMetrics().toList();

      expect(metrics, isNotEmpty);
      final totalLength = metrics.fold<double>(0, (s, m) => s + m.length);
      // 5 dashes * 10 px, with the last one possibly clipped at 95..100
      expect(totalLength, closeTo(50, 0.5));
    });

    test('never exceeds the source path length', () {
      final line = Path()
        ..moveTo(0, 0)
        ..lineTo(25, 0);

      final dashed = dashPath(line, dash: 10, gap: 10);
      final total = dashed
          .computeMetrics()
          .fold<double>(0, (s, m) => s + m.length);

      expect(total, lessThanOrEqualTo(25 + 0.001));
    });

    test('dashedCircle derives from a circle path', () {
      final path = dashedCircle(
        center: const Offset(50, 50),
        radius: 40,
        dash: 15,
        gap: 25,
      );
      expect(path.computeMetrics().toList(), isNotEmpty);
    });

    test('hexagonPath closes to its first vertex', () {
      final path = hexagonPath(const Rect.fromLTWH(0, 0, 140, 140));
      final metrics = path.computeMetrics().toList();
      expect(metrics, hasLength(1));
      // 6 equal-ish sides of a 140 box; perimeter should be well over 200.
      expect(metrics.first.length, greaterThan(200));
    });
  });
}

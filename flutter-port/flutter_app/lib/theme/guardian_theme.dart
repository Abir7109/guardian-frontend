import 'package:flutter/material.dart';

import 'colors.dart';
import 'tokens.dart';

/// Typography: every label <= 12 sp is UPPERCASE with >= 1.0 letter spacing.
/// That contrast against 26–32 sp ExtraBold headings is the visual identity.
abstract final class GuardianType {
  static const _family = 'Inter'; // bundle assets/fonts/Inter-*.ttf

  static TextStyle _s({
    required double size,
    required FontWeight weight,
    double ls = 0,
    double height = 1.3,
    Color color = GuardianColors.onSurface,
  }) =>
      TextStyle(
        fontFamily: _family,
        fontSize: size,
        fontWeight: weight,
        letterSpacing: ls,
        height: height,
        color: color,
      );

  // Display
  static final displayXl = _s(size: 32, weight: FontWeight.w800, ls: 8, height: 1.1);
  static final displayLg = _s(size: 32, weight: FontWeight.w800, ls: 2, height: 1.2);
  static final headline = _s(size: 28, weight: FontWeight.w800, ls: 1, height: 1.2);
  static final titleLg = _s(size: 26, weight: FontWeight.w800, height: 1.25);
  static final titleMd = _s(size: 24, weight: FontWeight.w800, ls: 1, height: 1.25);
  static final titleSm = _s(size: 18, weight: FontWeight.w800, height: 1.3);

  // Body
  static final body = _s(
    size: 14,
    weight: FontWeight.w500,
    height: 1.45,
    color: GuardianColors.onSurfaceVariant,
  );
  static final bodySm = _s(
    size: 13,
    weight: FontWeight.w400,
    height: 1.4,
    color: GuardianColors.onSurfaceVariant,
  );

  // Labels — the HUD voice
  static final label = _s(size: 12, weight: FontWeight.w700, ls: 1, height: 1.2);
  static final labelMicro = _s(
    size: 10,
    weight: FontWeight.w700,
    ls: 1.5,
    height: 1.2,
    color: GuardianColors.onSurfaceVariant,
  );
  static final labelNano = _s(
    size: 9,
    weight: FontWeight.w700,
    ls: 0.5,
    height: 1.2,
    color: GuardianColors.onSurfaceVariant,
  );

  /// Tabular figures for metrics/clock/keypad.
  static final mono = _s(size: 18, weight: FontWeight.w700, height: 1.2)
      .copyWith(fontFeatures: const [FontFeature.tabularFigures()]);
}

class GuardianTheme {
  static ThemeData dark() {
    const scheme = ColorScheme.dark(
      primary: GuardianColors.periwinkle,
      onPrimary: GuardianColors.onPeriwinkle,
      secondary: GuardianColors.cyan,
      onSecondary: GuardianColors.onCyan,
      tertiary: GuardianColors.alert,
      background: GuardianColors.voidBlack,
      surface: GuardianColors.slate,
      onSurface: GuardianColors.onSurface,
      onSurfaceVariant: GuardianColors.onSurfaceVariant,
      outline: GuardianColors.outline,
      outlineVariant: GuardianColors.outlineVariant,
      error: GuardianColors.alert,
      onError: GuardianColors.onAlert,
    );

    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: scheme,
      scaffoldBackgroundColor: GuardianColors.voidBlack,
      fontFamily: 'Inter',
      splashFactory: InkSparkle.splashFactory,
      dividerTheme: const DividerThemeData(
        color: GuardianColors.outlineVariant,
        thickness: GuardianRadius.hairline,
        space: GuardianRadius.hairline,
      ),
      textTheme: TextTheme(
        displayLarge: GuardianType.displayLg,
        displayMedium: GuardianType.headline,
        headlineSmall: GuardianType.titleLg,
        titleMedium: GuardianType.titleSm,
        bodyMedium: GuardianType.body,
        bodySmall: GuardianType.bodySm,
        labelMedium: GuardianType.label,
        labelSmall: GuardianType.labelNano,
      ),
      // NOTE: Flutter 3.47+ — package:flutter/material.dart is deprecated in the
      // Nov 2026 stable. Keep Material usage confined to this file + widgets so
      // `dart fix --apply --code=migrate_design_widgets` is a mechanical swap.
    );
  }
}

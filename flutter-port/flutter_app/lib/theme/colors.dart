import 'package:flutter/material.dart';

/// Guardian colour tokens — 1:1 with the Kotlin `Color.kt`.
/// Nothing outside `lib/theme/` may declare a raw colour.
abstract final class GuardianColors {
  // Surfaces
  static const voidBlack = Color(0xFF050507);
  static const surfaceLowest = Color(0xFF0E0E11);
  static const slate = Color(0xFF131316);
  static const surfaceLow = Color(0xFF1C1B1E);
  static const surfaceContainer = Color(0xFF201F22);
  static const surfaceBright = Color(0xFF39393C);

  // Accents
  static const cyan = Color(0xFF49FCD9);
  static const cyanDim = Color(0xFF04DFBD);
  static const mint = Color(0xFF4DFFDB);
  static const periwinkle = Color(0xFFB6C4FF);
  static const blue = Color(0xFF3D6FFF);
  static const violet = Color(0xFF6B4FFF);
  static const inputFocus = Color(0xFF668AFF);

  // Status
  static const alert = Color(0xFFFF5167);
  static const alertBright = Color(0xFFFF4560);
  static const warning = Color(0xFFFFB3B5);

  // Text & lines
  static const onSurface = Color(0xFFE5E1E5);
  static const onSurfaceVariant = Color(0xFFC3C5D8);
  static const outline = Color(0xFF8D90A1);
  static const outlineVariant = Color(0xFF434655);

  // On-colours
  static const onCyan = Color(0xFF00382E);
  static const onPeriwinkle = Color(0xFF00164F);
  static const onAlert = Color(0xFF5B0015);
  static const onCyanDeep = Color(0xFF00201A);
}

abstract final class GuardianGradients {
  static const cta = LinearGradient(
    colors: [GuardianColors.blue, GuardianColors.mint],
  );
  static const permissionTile = LinearGradient(
    colors: [GuardianColors.blue, GuardianColors.violet],
  );
  static const proBadge = LinearGradient(
    colors: [GuardianColors.alert, GuardianColors.warning],
  );
  static const avatarRing = SweepGradient(
    colors: [
      GuardianColors.cyan,
      GuardianColors.blue,
      GuardianColors.cyan,
    ],
  );

  /// Ambient radial glow. NOTE: Flutter's RadialGradient radius is fractional
  /// (relative to the paint box), unlike Compose's pixel radius.
  static RadialGradient glow(Color color, {double alpha = 0.08}) =>
      RadialGradient(
        colors: [color.withValues(alpha: alpha), Colors.transparent],
      );
}

import 'package:flutter/material.dart';

/// Spacing, radius, size and motion tokens measured from the Kotlin source.
abstract final class GuardianSpacing {
  static const xxs = 2.0;
  static const xs = 4.0;
  static const sm = 6.0;
  static const md = 8.0;
  static const lg = 10.0;
  static const xl = 12.0;
  static const xxl = 16.0;
  static const x3 = 20.0;
  static const x4 = 24.0;
  static const x5 = 32.0;
  static const x6 = 40.0;
  static const x7 = 48.0;

  /// Auth/onboarding screens use 24, main screens use 20.
  static const screenPadAuth = 24.0;
  static const screenPadMain = 20.0;
}

abstract final class GuardianRadius {
  static const chip = 8.0;
  static const tile = 10.0;
  static const button = 12.0;
  static const card = 14.0;
  static const sheet = 24.0;

  /// 34 of 34 borders in the Kotlin app are 0.5 dp. Do not round up.
  static const hairline = 0.5;
}

abstract final class GuardianSizes {
  static const primaryButton = 52.0;
  static const secondaryButton = 48.0;
  static const bottomNav = 80.0;
  static const radar = 240.0;
  static const core = 130.0;
  static const avatar = 90.0;
  static const pinKeypad = 288.0;
}

abstract final class GuardianMotion {
  static const screen = Duration(milliseconds: 400);
  static const shimmer = Duration(milliseconds: 1500);
  static const orbitSlow = Duration(seconds: 12);
  static const radarOuter = Duration(seconds: 24);
  static const radarInner = Duration(seconds: 16);
  static const breathe = Duration(milliseconds: 2000);
  static const heartbeat = Duration(milliseconds: 1500);
  static const avatarSpin = Duration(seconds: 4);
  static const shake = Duration(milliseconds: 600);
}

/// Coloured glow — Guardian never uses Material elevation for depth.
List<BoxShadow> guardianGlow(Color color, {double blur = 24, double alpha = 0.4}) =>
    [
      BoxShadow(
        color: color.withValues(alpha: alpha),
        blurRadius: blur,
        spreadRadius: 2,
      ),
    ];

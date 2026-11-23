import 'package:flutter/material.dart';

import '../../theme/colors.dart';
import '../../theme/tokens.dart';

/// The app's single card primitive.
/// Kotlin: `Card(containerColor = #131316 @ .6, border 0.5 dp #434655 @ .3, r14)`
/// — repeated 8+ times across Home/Profile/Permissions.
class GlassCard extends StatelessWidget {
  const GlassCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(GuardianSpacing.xxl),
    this.radius = GuardianRadius.card,
    this.onTap,
    this.borderAlpha = 0.3,
    this.color,
  });

  final Widget child;
  final EdgeInsetsGeometry padding;
  final double radius;
  final VoidCallback? onTap;
  final double borderAlpha;
  final Color? color;

  @override
  Widget build(BuildContext context) {
    final shape = RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(radius),
      side: BorderSide(
        color: GuardianColors.outlineVariant.withValues(alpha: borderAlpha),
        width: GuardianRadius.hairline,
      ),
    );

    final body = Padding(padding: padding, child: child);

    return Material(
      color: (color ?? GuardianColors.slate).withValues(alpha: 0.6),
      shape: shape,
      clipBehavior: Clip.antiAlias,
      child: onTap == null
          ? body
          : InkWell(onTap: onTap, splashColor: GuardianColors.cyan.withValues(alpha: 0.06), child: body),
    );
  }
}

/// 0.5 dp divider — used between settings rows.
class Hairline extends StatelessWidget {
  const Hairline({super.key, this.indent = 0});

  final double indent;

  @override
  Widget build(BuildContext context) => Padding(
        padding: EdgeInsets.only(left: indent),
        child: Divider(
          height: GuardianRadius.hairline * 2,
          thickness: GuardianRadius.hairline,
          color: GuardianColors.outlineVariant.withValues(alpha: 0.2),
        ),
      );
}

/// 36 dp rounded icon well that precedes nearly every settings row.
class IconWell extends StatelessWidget {
  const IconWell({super.key, required this.icon, this.size = 36, this.iconSize = 18});

  final IconData icon;
  final double size;
  final double iconSize;

  @override
  Widget build(BuildContext context) => Container(
        width: size,
        height: size,
        decoration: BoxDecoration(
          color: GuardianColors.surfaceContainer,
          borderRadius: BorderRadius.circular(GuardianRadius.chip),
        ),
        child: Icon(
          icon,
          size: iconSize,
          color: GuardianColors.onSurfaceVariant.withValues(alpha: 0.8),
        ),
      );
}

/// Pill badge — "ACTIVE", "PRO", "VERIFIED", "PENDING".
class HudChip extends StatelessWidget {
  const HudChip._({
    required this.label,
    required this.fg,
    this.bg,
    this.border,
    this.gradient,
    this.dot = false,
    this.fontSize = 9,
    this.padding = const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
  });

  factory HudChip.ok(String label) => HudChip._(
        label: label,
        fg: GuardianColors.cyan,
        bg: GuardianColors.cyan.withValues(alpha: 0.1),
        border: GuardianColors.cyan.withValues(alpha: 0.4),
        dot: true,
      );

  factory HudChip.pro(String label) => HudChip._(
        label: label,
        fg: GuardianColors.onAlert,
        gradient: GuardianGradients.proBadge,
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 5),
        fontSize: 10,
      );

  factory HudChip.outline(String label, Color color) => HudChip._(
        label: label,
        fg: color,
        bg: color.withValues(alpha: 0.05),
        border: color.withValues(alpha: 0.5),
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 5),
        fontSize: 10,
      );

  factory HudChip.alert(String label) => HudChip._(
        label: label,
        fg: GuardianColors.alert,
        bg: GuardianColors.alert.withValues(alpha: 0.15),
        border: GuardianColors.alert.withValues(alpha: 0.4),
      );

  final String label;
  final Color fg;
  final Color? bg;
  final Color? border;
  final Gradient? gradient;
  final bool dot;
  final double fontSize;
  final EdgeInsets padding;

  @override
  Widget build(BuildContext context) {
    final content = Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        if (dot) ...[
          Container(
            width: 6,
            height: 6,
            decoration: BoxDecoration(color: fg, shape: BoxShape.circle),
          ),
          const SizedBox(width: 4),
        ],
        Text(
          label.toUpperCase(),
          style: TextStyle(
            fontSize: fontSize,
            fontWeight: FontWeight.w700,
            letterSpacing: 0.8,
            color: fg,
          ),
        ),
      ],
    );

    return Container(
      padding: padding,
      decoration: BoxDecoration(
        color: bg,
        gradient: gradient,
        borderRadius: BorderRadius.circular(GuardianRadius.card * 2),
        border: border == null ? null : Border.all(color: border!, width: GuardianRadius.hairline),
      ),
      child: content,
    );
  }
}

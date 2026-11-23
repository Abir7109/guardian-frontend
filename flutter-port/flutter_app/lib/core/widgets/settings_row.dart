import 'package:flutter/material.dart';

import '../../theme/colors.dart';
import '../../theme/guardian_theme.dart';
import '../../theme/tokens.dart';
import 'glass_card.dart';

/// Section label — Kotlin `SettingsSectionHeader`.
class SectionHeader extends StatelessWidget {
  const SectionHeader(this.text, {super.key});

  final String text;

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.only(left: GuardianSpacing.xs, bottom: GuardianSpacing.md),
        child: Text(
          text.toUpperCase(),
          style: GuardianType.labelMicro.copyWith(
            color: GuardianColors.periwinkle,
            letterSpacing: 2,
          ),
        ),
      );
}

/// Settings row with three variants.
///
/// Kotlin had two separate composables — `SettingsToggleRow` and
/// `SettingsValueRow` — that duplicated the icon-well + label layout.
/// This merges them.
class SettingsRow extends StatelessWidget {
  const SettingsRow._({
    required this.icon,
    required this.title,
    this.subtitle,
    this.trailing,
    this.onTap,
  });

  /// Switch row.
  factory SettingsRow.toggle({
    required IconData icon,
    required String title,
    required bool value,
    required ValueChanged<bool> onChanged,
    String? subtitle,
    Key? key,
  }) =>
      SettingsRow._(
        key: key,
        icon: icon,
        title: title,
        subtitle: subtitle,
        trailing: Switch(
          value: value,
          onChanged: onChanged,
          activeThumbColor: GuardianColors.surfaceLowest,
          activeTrackColor: GuardianColors.cyan,
          inactiveThumbColor: GuardianColors.outline,
          inactiveTrackColor: GuardianColors.surfaceContainer,
        ),
      );

  /// Tappable row with a value + chevron.
  factory SettingsRow.value({
    required IconData icon,
    required String title,
    required String value,
    required VoidCallback onTap,
    String? subtitle,
    Key? key,
  }) =>
      SettingsRow._(
        key: key,
        icon: icon,
        title: title,
        subtitle: subtitle,
        onTap: onTap,
        trailing: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              value.toUpperCase(),
              style: GuardianType.label.copyWith(
                fontSize: 11,
                color: value.toUpperCase() == 'OFF'
                    ? GuardianColors.onSurfaceVariant.withValues(alpha: 0.5)
                    : GuardianColors.cyan,
              ),
            ),
            const SizedBox(width: GuardianSpacing.sm),
            const Icon(Icons.chevron_right, size: 18, color: GuardianColors.outline),
          ],
        ),
      );

  final IconData icon;
  final String title;
  final String? subtitle;
  final Widget? trailing;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final row = Padding(
      padding: const EdgeInsets.symmetric(
        horizontal: GuardianSpacing.xxl,
        vertical: GuardianSpacing.xl,
      ),
      child: Row(
        children: [
          IconWell(icon: icon),
          const SizedBox(width: GuardianSpacing.xxl),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: GuardianType.body.copyWith(
                    fontWeight: FontWeight.w600,
                    color: GuardianColors.onSurface,
                  ),
                ),
                if (subtitle != null) ...[
                  const SizedBox(height: 2),
                  Text(subtitle!, style: GuardianType.labelNano.copyWith(
                    color: GuardianColors.onSurfaceVariant.withValues(alpha: 0.6),
                  )),
                ],
              ],
            ),
          ),
          if (trailing != null) trailing!,
        ],
      ),
    );

    // The switch needs to be tappable on its own; only wrap the row when the
    // whole row navigates.
    return onTap == null
        ? Semantics(label: title, child: row)
        : Semantics(
            button: true,
            label: '$title, ${_valueSemantic()}',
            child: InkWell(
              onTap: onTap,
              child: row,
            ),
          );
  }

  String _valueSemantic() {
    final t = trailing;
    if (t == null) return '';
    return 'settings';
  }
}

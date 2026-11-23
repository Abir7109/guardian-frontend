import 'package:flutter/material.dart';

import '../../theme/colors.dart';
import '../../theme/guardian_theme.dart';
import '../../theme/tokens.dart';

/// Custom 80 dp bottom bar — Kotlin `GuardianBottomNavBar`.
/// Deliberately NOT Material `NavigationBar`: the active "pill" is the brand.
class GuardianBottomNav extends StatelessWidget {
  const GuardianBottomNav({
    super.key,
    required this.activeTab,
    required this.onHome,
    required this.onFeatures,
    required this.onProfile,
  });

  final int activeTab;
  final VoidCallback onHome;
  final VoidCallback onFeatures;
  final VoidCallback onProfile;

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      top: false,
      child: Container(
        height: GuardianSizes.bottomNav,
        decoration: BoxDecoration(
          color: GuardianColors.surfaceContainer.withValues(alpha: 0.6),
          border: Border(
            top: BorderSide(
              color: Colors.white.withValues(alpha: 0.1),
              width: GuardianRadius.hairline,
            ),
          ),
        ),
        padding: const EdgeInsets.symmetric(
          horizontal: GuardianSpacing.xxl,
          vertical: GuardianSpacing.md,
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: [
            _NavItem(
              icon: Icons.shield,
              label: 'HOME',
              active: activeTab == 0,
              onTap: onHome,
            ),
            _NavItem(
              icon: Icons.grid_view,
              label: 'ALERTS',
              active: activeTab == 1,
              onTap: onFeatures,
            ),
            _NavItem(
              icon: Icons.person,
              label: 'PROFILE',
              active: activeTab == 2,
              onTap: onProfile,
            ),
          ],
        ),
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  const _NavItem({
    required this.icon,
    required this.label,
    required this.active,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final bool active;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final color = active
        ? GuardianColors.cyan
        : GuardianColors.onSurfaceVariant.withValues(alpha: 0.6);

    final content = active
        ? Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: GuardianColors.cyan.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(GuardianRadius.button),
              border: Border.all(
                color: GuardianColors.cyan.withValues(alpha: 0.3),
                width: GuardianRadius.hairline,
              ),
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(icon, size: 20, color: color),
                const SizedBox(height: 2),
                Text(label, style: GuardianType.labelNano.copyWith(color: color)),
              ],
            ),
          )
        : Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(icon, size: 20, color: color),
              const SizedBox(height: 4),
              Text(label, style: GuardianType.labelNano.copyWith(color: color)),
            ],
          );

    return Semantics(
      button: true,
      selected: active,
      label: label,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(GuardianRadius.button),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: GuardianSpacing.xxl, vertical: 6),
          child: content,
        ),
      ),
    );
  }
}

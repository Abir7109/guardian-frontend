import 'package:flutter/material.dart';

import '../../core/widgets/buttons.dart';
import '../../core/widgets/glass_card.dart';
import '../../core/widgets/hud_animation_hub.dart';
import '../../theme/colors.dart';
import '../../theme/guardian_theme.dart';
import '../../theme/tokens.dart';

/// Armed state — the Kotlin screen always rendered "SYSTEM ARMED", which is a
/// false claim when no protection is running. This is the real state machine.
enum ArmedState { armed, disarmed, lockdown, offline, degraded }

extension ArmedStateX on ArmedState {
  String get label => switch (this) {
        ArmedState.armed => 'SYSTEM ARMED',
        ArmedState.disarmed => 'SYSTEM DISARMED',
        ArmedState.lockdown => 'LOCKDOWN',
        ArmedState.offline => 'OFFLINE',
        ArmedState.degraded => 'DEGRADED',
      };

  Color get color => switch (this) {
        ArmedState.armed => GuardianColors.cyan,
        ArmedState.disarmed => GuardianColors.outline,
        ArmedState.lockdown => GuardianColors.alert,
        ArmedState.offline => GuardianColors.warning,
        ArmedState.degraded => GuardianColors.periwinkle,
      };

  Color get statusChipColor => color;
}

class HomeStat {
  const HomeStat({
    required this.icon,
    required this.value,
    required this.caption,
    required this.color,
  });

  final IconData icon;
  final String? value; // null -> renders '—' (unavailable, never fabricated)
  final String caption;
  final Color color;
}

class HomeScreen extends StatelessWidget {
  const HomeScreen({
    super.key,
    required this.operatorName,
    required this.armedState,
    required this.stats,
    required this.safeZoneLabel,
    required this.onLockdown,
    required this.onAlerts,
    required this.onProfile,
    required this.onUpdateSafeZone,
    this.avatarAsset = 'assets/avatars/operator_alex.png',
    this.safeZoneActive = true,
    this.unreadAlerts = 0,
  });

  final String operatorName;
  final ArmedState armedState;
  final List<HomeStat> stats;
  final String safeZoneLabel;
  final VoidCallback onLockdown;
  final VoidCallback onAlerts;
  final VoidCallback onProfile;
  final VoidCallback onUpdateSafeZone;
  final String avatarAsset;
  final bool safeZoneActive;
  final int unreadAlerts;

  String get _greeting {
    final h = DateTime.now().hour;
    if (h < 12) return 'Good morning';
    if (h < 18) return 'Good afternoon';
    return 'Good evening';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: GuardianColors.voidBlack,
      body: SafeArea(
        bottom: false,
        child: CustomScrollView(
          slivers: [
            SliverPadding(
              padding: const EdgeInsets.symmetric(
                horizontal: GuardianSpacing.screenPadMain,
              ),
              sliver: SliverList.list(
                children: [
                  const SizedBox(height: GuardianSpacing.x3),
                  _OperatorHeader(
                    greeting: '$_greeting, $operatorName',
                    avatarAsset: avatarAsset,
                    unreadAlerts: unreadAlerts,
                    onAlerts: onAlerts,
                  ),
                  const SizedBox(height: GuardianSpacing.x5),
                  Center(
                    child: SystemCore(
                      label: armedState.label,
                      active: armedState == ArmedState.armed,
                      onTap: onLockdown,
                    ),
                  ),
                  const SizedBox(height: GuardianSpacing.x3),
                  Row(
                    children: [
                      for (var i = 0; i < stats.length; i++) ...[
                        if (i > 0) const SizedBox(width: GuardianSpacing.lg),
                        Expanded(child: _StatCard(stat: stats[i])),
                      ],
                    ],
                  ),
                  const SizedBox(height: GuardianSpacing.x3),
                  _SafeZoneCard(
                    label: safeZoneLabel,
                    active: safeZoneActive,
                    onUpdate: onUpdateSafeZone,
                  ),
                  const SizedBox(height: GuardianSpacing.x6),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _OperatorHeader extends StatelessWidget {
  const _OperatorHeader({
    required this.greeting,
    required this.avatarAsset,
    required this.unreadAlerts,
    required this.onAlerts,
  });

  final String greeting;
  final String avatarAsset;
  final int unreadAlerts;
  final VoidCallback onAlerts;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 44,
          height: 44,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: GuardianColors.slate,
            border: Border.all(color: GuardianColors.cyan, width: 2),
          ),
          child: ClipOval(
            child: Image.asset(
              avatarAsset,
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => const Icon(
                Icons.person,
                color: GuardianColors.onSurfaceVariant,
              ),
            ),
          ),
        ),
        const SizedBox(width: GuardianSpacing.xl),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                greeting,
                style: GuardianType.titleSm,
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              Text(
                'OPERATOR ACTIVE',
                style: GuardianType.labelNano.copyWith(
                  color: GuardianColors.cyan,
                  letterSpacing: 1,
                ),
              ),
            ],
          ),
        ),
        Semantics(
          button: true,
          label: unreadAlerts > 0
              ? 'Alerts, $unreadAlerts unread'
              : 'Alerts, none unread',
          child: InkWell(
            onTap: onAlerts,
            customBorder: const CircleBorder(),
            child: Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: GuardianColors.surfaceContainer.withValues(alpha: 0.6),
                border: Border.all(
                  color: GuardianColors.outlineVariant.withValues(alpha: 0.5),
                  width: GuardianRadius.hairline,
                ),
              ),
              child: Stack(
                alignment: Alignment.center,
                children: [
                  const Icon(
                    Icons.notifications_none,
                    size: 20,
                    color: GuardianColors.onSurface,
                  ),
                  if (unreadAlerts > 0)
                    Positioned(
                      top: 8,
                      right: 8,
                      child: Container(
                        width: 8,
                        height: 8,
                        decoration: const BoxDecoration(
                          color: GuardianColors.alert,
                          shape: BoxShape.circle,
                        ),
                      ),
                    ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class _StatCard extends StatelessWidget {
  const _StatCard({required this.stat});

  final HomeStat stat;

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      radius: GuardianRadius.button,
      borderAlpha: 0.3,
      padding: const EdgeInsets.symmetric(
        vertical: GuardianSpacing.xl,
        horizontal: GuardianSpacing.md,
      ),
      child: Column(
        children: [
          Icon(stat.icon, size: 20, color: stat.color),
          const SizedBox(height: GuardianSpacing.xs),
          Text(
            stat.value ?? '—',
            style: GuardianType.mono.copyWith(color: GuardianColors.onSurface),
          ),
          Text(
            stat.caption.toUpperCase(),
            style: GuardianType.labelNano.copyWith(
              fontSize: 9,
              color: GuardianColors.onSurfaceVariant.withValues(alpha: 0.6),
            ),
          ),
        ],
      ),
    );
  }
}

class _SafeZoneCard extends StatelessWidget {
  const _SafeZoneCard({
    required this.label,
    required this.active,
    required this.onUpdate,
  });

  final String label;
  final bool active;
  final VoidCallback onUpdate;

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      padding: const EdgeInsets.all(GuardianSpacing.xxl),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('Safe Zone', style: GuardianType.titleSm),
              active
                  ? HudChip.ok('Active')
                  : HudChip.alert('Inactive'),
            ],
          ),
          const SizedBox(height: GuardianSpacing.xxl),
          const SafeZoneMap(),
          const SizedBox(height: GuardianSpacing.md),
          Text(
            label,
            style: GuardianType.labelNano.copyWith(
              color: GuardianColors.outline,
            ),
          ),
          const SizedBox(height: GuardianSpacing.xxl),
          GuardianOutlineButton(
            label: 'Update Safe Location',
            icon: Icons.edit_location_alt_outlined,
            fill: GuardianColors.surfaceLowest.withValues(alpha: 0.5),
            height: 44,
            onPressed: onUpdate,
          ),
        ],
      ),
    );
  }
}

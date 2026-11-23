import 'package:flutter/material.dart';

import '../../theme/colors.dart';
import '../../theme/guardian_theme.dart';
import '../../theme/tokens.dart';
import 'glass_card.dart';

/// Audit-log row — Kotlin `LogItemRow`.
/// Status dot carries a coloured glow (Compose `shadow(ambientColor/spotColor)`).
class LogTile extends StatelessWidget {
  const LogTile({
    super.key,
    required this.authorized,
    required this.title,
    required this.node,
    required this.time,
    this.onTap,
  });

  final bool authorized;
  final String title;
  final String node;
  final String time;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    final color = authorized ? GuardianColors.cyan : GuardianColors.alert;
    return Semantics(
      label: '$title, $node, $time, ${authorized ? 'authorized' : 'blocked'}',
      child: GlassCard(
        radius: GuardianRadius.tile,
        borderAlpha: 0.2,
        padding: const EdgeInsets.all(GuardianSpacing.xxl),
        onTap: onTap,
        child: Row(
          children: [
            Container(
              width: 8,
              height: 8,
              decoration: BoxDecoration(
                color: color,
                shape: BoxShape.circle,
                boxShadow: guardianGlow(color, blur: 8, alpha: 0.6),
              ),
            ),
            const SizedBox(width: GuardianSpacing.xl),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: GuardianType.body.copyWith(
                      fontWeight: FontWeight.w700,
                      color: GuardianColors.onSurface,
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    node.toUpperCase(),
                    style: GuardianType.labelNano.copyWith(
                      color: GuardianColors.onSurfaceVariant.withValues(alpha: 0.4),
                    ),
                  ),
                ],
              ),
            ),
            Text(
              time,
              style: GuardianType.label.copyWith(
                fontSize: 11,
                color: GuardianColors.outline,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

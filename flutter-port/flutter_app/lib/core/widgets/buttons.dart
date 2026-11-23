import 'package:flutter/material.dart';

import '../../theme/colors.dart';
import '../../theme/tokens.dart';

/// Gradient CTA — the login "SIGN IN" and permissions "GRANT ACCESS" button.
///
/// Kotlin wrapped a gradient `Box` inside a transparent `Button`; Flutter can't
/// style a Material button with a gradient, so this is a purpose-built widget.
class PrimaryGradientButton extends StatelessWidget {
  const PrimaryGradientButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.icon,
    this.loading = false,
    this.height = GuardianSizes.primaryButton,
    this.gradient = GuardianGradients.cta,
    this.letterSpacing = 1,
  });

  final String label;
  final VoidCallback? onPressed;
  final IconData? icon;
  final bool loading;
  final double height;
  final Gradient gradient;
  final double letterSpacing;

  @override
  Widget build(BuildContext context) {
    final enabled = onPressed != null && !loading;
    return Semantics(
      button: true,
      enabled: enabled,
      label: label,
      child: Opacity(
        opacity: enabled ? 1 : 0.55,
        child: SizedBox(
          height: height,
          width: double.infinity,
          child: DecoratedBox(
            decoration: BoxDecoration(
              gradient: gradient,
              borderRadius: BorderRadius.circular(GuardianRadius.button),
            ),
            child: Material(
              color: Colors.transparent,
              borderRadius: BorderRadius.circular(GuardianRadius.button),
              child: InkWell(
                onTap: enabled ? onPressed : null,
                borderRadius: BorderRadius.circular(GuardianRadius.button),
                child: Center(
                  child: loading
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(
                            strokeWidth: 2,
                            valueColor:
                                AlwaysStoppedAnimation(GuardianColors.onPeriwinkle),
                          ),
                        )
                      : Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            if (icon != null) ...[
                              Icon(icon, size: 18, color: GuardianColors.onPeriwinkle),
                              const SizedBox(width: GuardianSpacing.md),
                            ],
                            Text(
                              label.toUpperCase(),
                              style: TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.w700,
                                letterSpacing: letterSpacing,
                                color: GuardianColors.onPeriwinkle,
                              ),
                            ),
                          ],
                        ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/// Ghost / outline button — "UPDATE SAFE LOCATION", "Continue", "DISCONNECT".
class GuardianOutlineButton extends StatelessWidget {
  const GuardianOutlineButton({
    super.key,
    required this.label,
    required this.onPressed,
    this.icon,
    this.color = GuardianColors.onSurface,
    this.borderColor,
    this.height = GuardianSizes.secondaryButton,
    this.fill,
  });

  final String label;
  final VoidCallback? onPressed;
  final IconData? icon;
  final Color color;
  final Color? borderColor;
  final double height;
  final Color? fill;

  @override
  Widget build(BuildContext context) {
    return Semantics(
      button: true,
      enabled: onPressed != null,
      label: label,
      child: SizedBox(
        height: height,
        width: double.infinity,
        child: OutlinedButton(
          onPressed: onPressed,
          style: OutlinedButton.styleFrom(
            foregroundColor: color,
            backgroundColor: fill,
            side: BorderSide(
              color: borderColor ?? GuardianColors.outlineVariant,
              width: GuardianRadius.hairline,
            ),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(GuardianRadius.button),
            ),
            padding: EdgeInsets.zero,
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (icon != null) ...[
                Icon(icon, size: 16, color: color),
                const SizedBox(width: GuardianSpacing.md),
              ],
              Text(
                label.toUpperCase(),
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: FontWeight.w700,
                  letterSpacing: 1,
                  color: color,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

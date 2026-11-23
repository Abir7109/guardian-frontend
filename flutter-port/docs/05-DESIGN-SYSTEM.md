# Design System — Guardian (Flutter)

Extracted from `app/src/main/java/com/example/ui/theme/Color.kt` + measured from `MainActivity.kt`. This is the **single source of truth** for the port; nothing outside `lib/theme/` may declare a colour.

## 1. Colour tokens

```dart
abstract final class GuardianColors {
  // ── Core surfaces (dark-only brand) ────────────────────────────────
  static const voidBlack        = Color(0xFF050507); // scaffold background
  static const surfaceLowest    = Color(0xFF0E0E11); // map bg, keypad wells
  static const slate            = Color(0xFF131316); // cards, inputs
  static const surfaceLow       = Color(0xFF1C1B1E);
  static const surfaceContainer = Color(0xFF201F22); // nav bar, icon wells
  static const surfaceBright    = Color(0xFF39393C);

  // ── Accents ────────────────────────────────────────────────────────
  static const cyan             = Color(0xFF49FCD9); // primary "ok/active"
  static const cyanDim          = Color(0xFF04DFBD);
  static const mint             = Color(0xFF4DFFDB); // gradient end
  static const periwinkle       = Color(0xFFB6C4FF); // secondary
  static const blue             = Color(0xFF3D6FFF); // gradient start
  static const violet           = Color(0xFF6B4FFF); // permission icon grad
  static const inputFocus       = Color(0xFF668AFF);

  // ── Status ─────────────────────────────────────────────────────────
  static const alert            = Color(0xFFFF5167); // breach / lockdown
  static const alertBright      = Color(0xFFFF4560);
  static const warning          = Color(0xFFFFB3B5);
  static const success          = Color(0xFF49FCD9);

  // ── Text & lines ───────────────────────────────────────────────────
  static const onSurface        = Color(0xFFE5E1E5);
  static const onSurfaceVariant = Color(0xFFC3C5D8);
  static const outline          = Color(0xFF8D90A1);
  static const outlineVariant   = Color(0xFF434655); // the ubiquitous 0.5dp hairline

  // ── On-colour (for fills) ──────────────────────────────────────────
  static const onCyan           = Color(0xFF00382E);
  static const onPeriwinkle     = Color(0xFF00164F);
  static const onAlert          = Color(0xFF5B0015);
  static const onCyanDeep       = Color(0xFF00201A);
}
```

### Opacity ramp (used everywhere instead of new hex values)
`0.03 · 0.05 · 0.08 · 0.10 · 0.12 · 0.15 · 0.20 · 0.25 · 0.30 · 0.40 · 0.50 · 0.60 · 0.70 · 0.80`
Rule: anything under 40 % opacity is decoration; ≥60 % is content. Never invent a value between steps.

### Semantic mapping for `ColorScheme`
| ColorScheme slot | Token |
|---|---|
| `primary` | periwinkle |
| `onPrimary` | onPeriwinkle |
| `secondary` | cyan |
| `onSecondary` | onCyan |
| `tertiary` | alert |
| `background` / `surface` | voidBlack / slate |
| `onBackground` / `onSurface` | onSurface |
| `onSurfaceVariant` | onSurfaceVariant |
| `outline` / `outlineVariant` | outline / outlineVariant |
| `error` | alert |

## 2. Gradients

```dart
abstract final class GuardianGradients {
  // Primary CTA (login, grant access)
  static const cta = LinearGradient(colors: [Color(0xFF3D6FFF), Color(0xFF4DFFDB)]);
  // Permission icon tile
  static const permissionTile = LinearGradient(colors: [Color(0xFF3D6FFF), Color(0xFF6B4FFF)]);
  // PRO badge
  static const proBadge = LinearGradient(colors: [Color(0xFFFF5167), Color(0xFFFFB3B5)]);
  // Rotating avatar ring
  static const avatarRing = SweepGradient(colors: [cyan, blue, cyan]);
  // Ambient glows (RadialGradient, radius is FRACTIONAL in Flutter)
  static RadialGradient glow(Color c, {double alpha = .08}) =>
      RadialGradient(colors: [c.withValues(alpha: alpha), Colors.transparent]);
}
```

## 3. Typography

The Kotlin app has **no custom font** — it relies on `FontFamily.Default` with aggressive weight/letter-spacing. Two options:

- **A (faithful):** `Inter` (bundled TTF) — closest free match to Roboto's HUD temperament, excellent at 9–11 sp.
- **B (branded):** `Space Grotesk` for headings + `JetBrains Mono` for telemetry numbers (SATS/COVERAGE/clock). Recommended — the mono numerals materially improve the HUD feel.

| Token | Size / Weight / LS / Height | Usage in app |
|---|---|---|
| `displayXl` | 32 / w800 / 8.0 / 1.1 | "GUARDIAN" splash |
| `displayLg` | 32 / w800 / 2.0 / 1.2 | "DEVICE LOCKED", "Welcome Back." |
| `headline` | 28 / w800 / 1.0 / 1.2 | operator name |
| `titleLg` | 26 / w800 / 0 / 1.25 | onboarding title |
| `titleMd` | 24 / w800 / 1.0 / 1.25 | "PERMISSIONS REQUIRED" |
| `titleSm` | 18 / w800 / 0 / 1.3 | "Safe Zone", greeting |
| `body` | 14 / w500 / 0 / 1.45 | descriptions |
| `bodySm` | 13 / w400 / 0 / 1.4 | FAQ answer |
| `label` | 12 / w700 / 1.0 / 1.2 | "SIGN IN", "Step 2 of 6" |
| `labelMicro` | 10 / w700 / 1.5 / 1.2 | section headers, "SYSTEM STATE" |
| `labelNano` | 9 / w700 / 0.5 / 1.2 | nav labels, stat captions |
| `mono` | tabular figures | digits on keypad, clock, metrics |

**Signature rule:** every label ≤12 sp is **UPPERCASE with ≥1.0 letter spacing**. That contrast against the 26–32 sp ExtraBold headings *is* the visual identity — preserve it exactly.

## 4. Spacing, radius, elevation

```dart
abstract final class GuardianSpacing {
  static const xxs = 2.0, xs = 4.0, sm = 6.0, md = 8.0, lg = 10.0,
               xl = 12.0, xxl = 16.0, x3 = 20.0, x4 = 24.0, x5 = 32.0,
               x6 = 40.0, x7 = 48.0;
  static const screenPadAuth = 24.0;   // splash/onboarding/login/permissions
  static const screenPadMain = 20.0;   // home/profile
}
abstract final class GuardianRadius {
  static const chip = 8.0, tile = 10.0, button = 12.0, card = 14.0, sheet = 24.0;
  static const hairline = 0.5;         // 34/34 borders use this — do not round up
}
abstract final class GuardianSizes {
  static const primaryButton = 52.0, secondaryButton = 48.0, bottomNav = 80.0;
  static const radar = 240.0, core = 130.0, avatar = 90.0, pinKeypad = 288.0;
}
```

**Glow, not elevation.** The app never uses Material elevation; depth is expressed as coloured shadow:
```dart
// Compose: Modifier.shadow(elevation=20.dp, ambientColor=cyan, spotColor=cyan)
const BoxShadow(color: Color(0x6649FCD9), blurRadius: 24, spreadRadius: 2)
```

## 5. Motion

```dart
abstract final class GuardianMotion {
  static const screen = Duration(milliseconds: 400);   // AnimatedContent fade
  static const shimmer = Duration(milliseconds: 1500); // linear, repeat
  static const orbitSlow = Duration(seconds: 12);      // onboarding
  static const radarOuter = Duration(seconds: 24);
  static const radarInner = Duration(seconds: 16);     // reverse
  static const breathe = Duration(milliseconds: 2000);  // easeInOutSine, reverse
  static const heartbeat = Duration(milliseconds: 1500); // easeInOutSine, reverse
  static const avatarSpin = Duration(seconds: 4);
  static const shake = Duration(milliseconds: 600);     // on error only
}
```
**Rules:** linear for rotation; `easeInOutSine` for breathing; `easeOutQuad` for expanding pulses; springy only on error. Never animate more than 3 independent controllers per screen — consolidate with `Interval` on one controller where possible.

## 6. Widget primitives to build (and their Kotlin origin)

| Flutter widget | Kotlin source | Notes |
|---|---|---|
| `GlassCard` | repeated `Card(containerColor = slate α.6, border 0.5 outlineVariant α.3, r14)` | Appears 8+ times — one widget |
| `Hairline` | `Divider(0.5.dp, outlineVariant α.2)` | — |
| `PrimaryGradientButton` | gradient `Box` inside transparent `Button` | Flutter needs a custom widget; `ElevatedButton` can't host a gradient cleanly |
| `GhostButton` | `OutlinedButton(0.5 dp outlineVariant)` | — |
| `DangerButton` | `OutlinedButton(alert α.5, fill α.05)` | — |
| `HudChip` | "ACTIVE"/"PRO"/"VERIFIED" pills | variants: `ok`, `alert`, `neutral`, `pro` |
| `SectionHeader` | `SettingsSectionHeader` | 10 sp/700/ls2/periwinkle |
| `SettingsRow` | `SettingsToggleRow` + `SettingsValueRow` | sealed variants: `.toggle`, `.value`, `.destructive` |
| `LogTile` | `LogItemRow` | status dot w/ coloured glow |
| `StatCard` | the 3 bento cards | icon + value + caption, equal flex |
| `IconWell` | 36 dp r8 `surfaceContainer` behind row icons | Appears 5+ times |
| `ScanlineOverlay` | *(new)* | subtle 1 px lines at 3 % for the HUD texture — optional, 1 flag to disable |
| `GuardianBottomNav` | `GuardianBottomNavBar` + `NavBarItem` | pill-style active state |

## 7. Asset map (what's bundled in this pack)

| Asset | Use |
|---|---|
| `assets/branding/guardian_logo.png` | App icon, splash mark, store listing |
| `assets/illustrations/onboarding_orbital.png` | Onboarding page 1 fallback / marketing |
| `assets/illustrations/safe_zone_map.png` | Safe Zone card backdrop (replaces unstable remote URL) |
| `assets/illustrations/lockdown_hero.png` | Lockdown screen marketing + empty state |
| `assets/stickers/*.png` | App Store screenshots, onboarding accents, social/marketing |
| `assets/avatars/operator_*.png` | Demo operator portraits (replaces `lh3.googleusercontent.com`) |

**Note:** all UI-critical graphics (radar, hexagon, orbit, geofence) should stay **code-drawn** (`CustomPainter`) for crispness at any density. These raster assets are for branding, empty states and store material — not as replacements for the painters.

## 8. Asset export checklist for production

- [ ] Re-export logo as **SVG** (the PNG is a raster reference) → `flutter_svg`
- [ ] Generate platform icons: Android adaptive (foreground/background/monochrome at 5 densities + `anydpi-v26`), iOS AppIcon set 20→1024
- [ ] Generate splash via `flutter_native_splash` from `guardian_logo` + `#050507`
- [ ] Compress PNGs (no UI asset should exceed ~400 KB) or convert photos to WebP
- [ ] Provide `2x/3x` densities for every raster
- [ ] Stickers: re-cut with true alpha (these have white backgrounds — fine for store screenshots, not for in-app overlays)

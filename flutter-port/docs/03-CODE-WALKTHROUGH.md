# Code Walkthrough: Old File → New Files

`MainActivity.kt` is 2,740 lines with 22 top-level declarations. This is exactly where each one goes. Treat it as a **work-order checklist**.

## 1. Declaration index (from the real file)

| # | Line | Declaration | Lines of code | Destination |
|---|---|---|---|---|
| 1 | 65 | `enum class GuardianScreen` | 8 | `router/app_router.dart` (as route paths) |
| 2 | 76 | `GuardianApp()` | 128 | `app.dart` + `router/app_router.dart` |
| 3 | 205 | `SplashScreen()` | 149 | `features/splash/splash_screen.dart` |
| 4 | 355 | `OnboardingScreen()` | 257 | `features/onboarding/onboarding_screen.dart` + `_page_orbital.dart` |
| 5 | 613 | `LoginScreen()` | 297 | `features/auth/login_screen.dart` |
| 6 | 911 | `PermissionsScreen()` | 366 | `features/permissions/permissions_screen.dart` + `_step_card.dart` |
| 7 | 1278 | `HomeScreen()` | 514 | `features/home/home_screen.dart` + `_system_core.dart` + `_stat_card.dart` + `_safe_zone_card.dart` |
| 8 | 1793 | `ProfileScreen()` | 337 | `features/profile/profile_screen.dart` + `_operator_header.dart` |
| 9 | 2131 | `SettingsSectionHeader()` | 13 | `core/widgets/section_header.dart` |
| 10 | 2145 | `SettingsToggleRow()` | 52 | `core/widgets/settings_row.dart` |
| 11 | 2198 | `SettingsValueRow()` | 64 | `core/widgets/settings_row.dart` |
| 12 | 2263 | `LogItemRow()` | 65 | `core/widgets/log_tile.dart` |
| 13 | 2329 | `AlarmOverlayScreen()` | 301 | `features/lockdown/lockdown_screen.dart` + `_pin_dots.dart` + `_keypad.dart` |
| 14 | 2631 | `GuardianBottomNavBar()` | 45 | `features/shell/guardian_bottom_nav.dart` |
| 15 | 2677 | `NavBarItem()` | 64 | `features/shell/guardian_bottom_nav.dart` |

Largest three (`HomeScreen` 514, `PermissionsScreen` 366, `ProfileScreen` 337) are also the highest-risk — budget extra QA.

## 2. Colour literals → tokens

The file contains **246** `Color(0x…)` occurrences. Distribution to preserve:

| Hex | Uses | Token |
|---|---|---|
| `0xFF49FCD9` | 57 | `GuardianColors.cyan` (secondary/glow) |
| `0xFF434655` | 34 | `GuardianColors.outlineVariant` |
| `0xFFFF5167` | 25 | `GuardianColors.alert` (tertiaryContainer) |
| `0xFFE5E1E5` | 25 | `GuardianColors.onSurface` |
| `0xFFC3C5D8` | 22 | `GuardianColors.onSurfaceVariant` |
| `0xFFB6C4FF` | 21 | `GuardianColors.periwinkle` (primary) |
| `0xFF131316` | 19 | `GuardianColors.slate` (surface) |
| `0xFF050507` | 14 | `GuardianColors.voidBlack` (background) |
| `0xFF8D90A1` | 9 | `GuardianColors.outline` |
| `0xFF3D6FFF` | 6 | `GuardianColors.blue` (gradient start) |
| `0xFF201F22` | 5 | `GuardianColors.surfaceContainer` |
| `0xFF0E0E11` | 4 | `GuardianColors.surfaceLowest` |
| `0xFF4DFFDB` | 2 | `GuardianColors.mint` (gradient end) |
| `0xFF668AFF`, `0xFF6B4FFF`, `0xFF0C0E1A`, `0xFF00201A`, `0xFF5B0015`, `0xFF002014`, `0xFFFF4560` | 1–2 each | keep as named constants in `colors.dart` |

**Migration technique:** don't hand-edit 246 sites. Write the token file first, then use a scripted find/replace (the hex → token table above), then delete every remaining `Color(0x` by making `flutter analyze` fail on it via a custom lint rule. Target: **zero** raw hex outside `theme/colors.dart`.

## 3. Spacing audit (measured from source)

Recurring values — codify these as `GuardianSpacing` instead of ad-hoc `.dp`:

- Screen horizontal padding: **24 dp** (splash/login/onboarding/permissions), **20 dp** (home/profile)
- Card inner padding: **16 dp**
- Card corner radii: **14 dp** (cards), **12 dp** (buttons/rows), **10 dp** (map/logs), **8 dp** (chips)
- Gaps between cards: **20 dp**; inside cards: **12–16 dp**
- Primary button height: **52 dp**; secondary: **48 dp**
- Bottom nav height: **80 dp**
- Icon sizes: **20 / 18 / 16 / 14 / 12 dp** (the app has a real icon scale — keep it)
- Hairline borders: **0.5 dp** almost everywhere (34 of 34 borders) — this is a stylistic signature; do not round to 1 dp

## 4. Copy inventory (must move to `AppStrings`)

~85 user-visible strings. All currently inline. Highlights worth flagging for tone/legal review:

- `"Premium military-grade protection for your mobile lifestyle."` — marketing claim; "military-grade" is an FTC-risky phrase in the US. Recommend softening.
- `"Guardian relies on critical alerts to keep you informed of tactical updates and security breaches."` — **must be rewritten** for the Play Store permission rationale (see `01-FEATURE-RESEARCH.md §F4`); Google requires plain-language purpose.
- `"OPERATIVE ID: 893-X-11"`, `"NODE: OMEGA-4"`, `"NODE: EXTERNAL-IP"` — flavour text, fine as demo data, must not be presented as real in production.
- `"SATS 12"` — implies GPS satellites locked. If not real, this is a fabricated telemetry claim. Gate behind real `GnssStatus` or relabel.
- `"v2.4.0"` hardcoded on Splash — must come from `PackageInfo.versionName`.
- `"Good evening, John"` — hardcoded time-of-day greeting that says "evening" at 9 a.m. Derive from `DateTime.now().hour`.
- `"ALEX VANCE"` on Profile vs `"John"` on Home — **two different names for the same operator.** Pick one (this is an actual inconsistency in the source).

## 5. Bugs present in the source — do **not** port

| Bug | Location | Fix in Flutter |
|---|---|---|
| 2FA row label stuck on `"OFF"` regardless of toggle | Profile `SettingsValueRow(value = "OFF")` L~1990 | Derive from state |
| Greeting/name mismatch (John vs Alex Vance) | Home L~1370 / Profile L~1855 | Single `userProvider` |
| `"01 / 03"` but only one onboarding page | Onboarding | 3-page `PageView` |
| `"Step 2 of 6"` but nothing is requested | Permissions | Real wizard |
| `"SYSTEM ARMED"` always shown, even though nothing is armed | Home L~1470 | State machine |
| Dead `FEATURES` nav tab | Bottom nav | Alerts tab |
| Pre-filled fake credentials in login fields | `GuardianApp` L80–81 | Empty + no autofill of secrets |
| `v2.4.0` hardcoded | Splash | `PackageInfo` |
| `isLockedError` reset before the user can see it (state flips in ~300 ms) | Lockdown L~2380 | Keep error visible ≥1.2 s or until next keypress |
| Unused imports (`Build`, `isSystemInDarkTheme`, `dynamic*ColorScheme`, `lightColorScheme`, `LocalContext`) | `Theme.kt` | n/a — clean from day one |
| `GreetingScreenshotTest` references a non-existent `Greeting()` | `app/src/test/…` | Write goldens against **real** screens |
| Screenshot test asserts `"My Application"` but string is `"Guardian"` | `ExampleRobolectricTest` | Same |
| No `INTERNET` permission while Coil loads remote images | `AndroidManifest.xml` | iOS/Android config handled by plugins; bundle assets instead |

## 6. Suggested build order (dependency-aware)

```
colors.dart → spacing.dart → typography.dart → motion.dart
      ↓
guardian_theme.dart
      ↓
core/widgets: SectionHeader, SettingsRow, LogTile, PrimaryGradientButton,
              GhostButton, HudChip, GlassCard, ScanlineOverlay
      ↓
core/painters: dash.dart (helper) → DashedCircle → RadarPainter → HexagonPainter
              → OrbitalPainter → SafeZoneMapPainter → AvatarSweepPainter
      ↓
shell: GuardianBottomNav + router
      ↓
splash → onboarding → login → permissions
      ↓
home (needs RadarPainter + StatCard + SafeZoneCard)
      ↓
profile (needs SettingsRow + LogTile + AvatarSweepPainter)
      ↓
lockdown (needs DashCircle + keypad + PIN service)
```

Painters before screens: `HomeScreen` depends on three of them, and getting dash math wrong at the end forces a re-test of every screen.

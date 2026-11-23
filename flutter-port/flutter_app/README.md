# guardian — Flutter port (scaffold)

This is the **working seed** of the Flutter rebuild, not the finished app. It contains the parts that are expensive to get wrong: design tokens, the dash-path helper, all custom painters, the shared widget library, the bottom nav, and two complete reference screens (Home + Lockdown) written against the real specs.

## What's here vs. what's missing

| Done | Status |
|---|---|
| `lib/theme/` — colours, spacing, radius, sizes, motion, typography, ThemeData | ✅ complete |
| `lib/core/painters/dash.dart` — the Flutter equivalent of `PathEffect.dashPathEffect` | ✅ complete + tested |
| `lib/core/painters/radar_painter.dart` — radar, splash radar, lockdown orbit | ✅ complete |
| `lib/core/painters/safe_zone_painter.dart` — geofence overlay, avatar sweep, hexagon, sweep arc | ✅ complete |
| `lib/core/widgets/` — GlassCard, Hairline, IconWell, HudChip, buttons, SettingsRow, SectionHeader, LogTile | ✅ complete |
| `lib/core/widgets/hud_animation_hub.dart` — one controller replacing 6 infinite transitions | ✅ complete |
| `lib/features/shell/guardian_bottom_nav.dart` | ✅ complete |
| `lib/features/home/home_screen.dart` | ✅ reference implementation |
| `lib/features/lockdown/lockdown_screen.dart` | ✅ reference implementation (real PIN contract) |
| `pubspec.yaml` | ✅ pinned stack |
| splash / onboarding / login / permissions / profile screens | ⬜ next (specs in `../docs/04-…`) |
| router, Riverpod providers, data layer | ⬜ next |
| assets/fonts/Inter-*.ttf | ⬜ download (see below) |

## Run it

```bash
# from this directory
mkdir -p assets/fonts   # add Inter-*.ttf, or drop the fonts: block and use google_fonts
flutter pub get
flutter run
```

Fonts are referenced in `pubspec.yaml`. Either drop the five Inter TTFs into `assets/fonts/`, or delete the `fonts:` block and rely on `google_fonts` (already a dependency).

Assets are expected at `assets/…` — copy the sibling `../assets/` folder in, or symlink it:

```bash
ln -s ../../assets assets
```

## Deliberate improvements over the Kotlin source

- **`RadarPainter` rotates via canvas transform** instead of `Modifier.rotate` on a container — same look, no extra layer.
- **One `AnimationController` set** in `HudAnimationHub` instead of six `rememberInfiniteTransition`s; respects `MediaQuery.disableAnimations`.
- **`HomeScreen` takes an `ArmedState`** so the core button can never claim "SYSTEM ARMED" when nothing is armed.
- **`HomeStat.value` is nullable** → renders `—` rather than a fabricated number.
- **`LockdownScreen` depends on a `PinVerifier` interface**, so the hardcoded `1234` cannot come back; includes lockout, biometric fallback, `PopScope(canPop: false)`.
- **All colours come from tokens** — zero raw hex outside `lib/theme/`.

## Known follow-ups

1. `SettingsRow`'s `_valueSemantic()` is a stub — pass the value in for proper screen-reader output.
2. `SafeZoneMapPainter._bracket()` has a leftover `assert(far != 0)`; simplify when adding the second corner style.
3. `drift`/`freezed` codegen not yet wired (`build_runner` not run — no generated files committed).
4. Golden tests only cover `dashPath`; screen goldens need `alchemist` setup + fonts bundled.
5. No `analysis_options.yaml` yet — add `very_good_analysis` and a `forbidden-Color` custom lint.

# Guardian → Flutter Port Kit

Everything needed to rebuild the Android/Kotlin app `guardian-frontend` in Flutter: research, a detailed plan, a screen-by-screen spec, a working code scaffold, and a visual asset pack.

**Read in this order:**

| # | File | What it answers |
|---|---|---|
| 1 | [`docs/00-MASTER-PLAN.md`](docs/00-MASTER-PLAN.md) | What "porting" means here, stack choices, 8-sprint plan with effort estimates, definition of done, decisions needed from you |
| 2 | [`docs/01-FEATURE-RESEARCH.md`](docs/01-FEATURE-RESEARCH.md) | What the app **is** vs what it *claims*, category research (5-stage anti-theft model), and how each feature is rebuilt (F1–F10) |
| 3 | [`docs/02-COMPOSE-TO-FLUTTER-MAP.md`](docs/02-COMPOSE-TO-FLUTTER-MAP.md) | Widget-by-widget, modifier-by-modifier translation tables + the 10 traps that will bite |
| 4 | [`docs/03-CODE-WALKTHROUGH.md`](docs/03-CODE-WALKTHROUGH.md) | Every declaration in the 2,740-line `MainActivity.kt` → its destination file; colour/spacing/copy audits; bugs not to port |
| 5 | [`docs/04-SCREEN-BY-SCREEN-REBUILD.md`](docs/04-SCREEN-BY-SCREEN-REBUILD.md) | Per-screen: purpose, Flutter tree, states, measurements, acceptance criteria |
| 6 | [`docs/05-DESIGN-SYSTEM.md`](docs/05-DESIGN-SYSTEM.md) | Colour tokens, type scale, spacing/radius, motion timings, widget primitives, asset map |
| 7 | [`docs/06-TESTING-QA-PARITY.md`](docs/06-TESTING-QA-PARITY.md) | Test pyramid, golden list, visual-parity method, device matrix, performance & a11y gates |
| 8 | [`docs/07-RISKS-AND-DECISIONS.md`](docs/07-RISKS-AND-DECISIONS.md) | 12 ranked risks, 10 Compose→Flutter traps, 8 blocking decisions |

## What's in the box

```
flutter-port/
├─ README.md                     ← you are here
├─ docs/                         ← 8 documents (above)
├─ flutter_app/                  ← working code scaffold
│  ├─ pubspec.yaml               ← pinned stack
│  ├─ lib/theme/                 ← colours, tokens, typography, ThemeData (complete)
│  ├─ lib/core/painters/         ← dash helper + radar/splash/lockdown/safezone/avatar/hexagon (complete)
│  ├─ lib/core/widgets/          ← GlassCard, HudChip, buttons, SettingsRow, LogTile, animation hub (complete)
│  ├─ lib/features/shell/        ← bottom nav (complete)
│  ├─ lib/features/home/         ← reference screen, real armed-state machine
│  ├─ lib/features/lockdown/     ← reference screen, real PIN contract + lockout
│  └─ test/dash_path_test.dart   ← tests for the riskiest drawing code
└─ assets/                       ← visual pack
   ├─ branding/guardian_logo.png
   ├─ illustrations/onboarding_orbital.png, safe_zone_map.png, lockdown_hero.png
   ├─ stickers/sticker_{shield,breach,locked,safezone,biometric,ai}.png
   └─ avatars/                   ← (operator portrait — pending, see note below)
```

## The one-paragraph summary

The Kotlin app is a **high-fidelity UI prototype** — the visual language is genuinely strong and worth preserving exactly, but every feature is a facade (login accepts anything, PIN is `1234`, permissions request nothing, metrics are string literals, Gemini is imported but never called). The Flutter port should carry over **100 % of the aesthetics, 0 % of the fakery**. Everything is a rewrite because there is no state layer, no backend, and no data layer to convert. Plan for ~41 dev-days to a shippable v1, or ~18 to a visual-parity MVP.

## Asset note

10 of 13 planned images were generated (a per-turn generation cap was hit). Delivered: the logo, 3 illustrations and 6 stickers. **Still to generate:** the operator avatar (`assets/avatars/operator_alex.png`), an empty-state bell illustration, and a splash backdrop. Until then `HomeScreen` falls back to a person glyph for the avatar, and `SafeZoneMap` degrades gracefully if the asset is missing.

All UI-critical graphics (radar, hexagon, orbit rings, geofence, PIN dots) are **code-drawn** via `CustomPainter` — the raster assets are for branding, empty states and store screenshots, never as replacements for the painters.

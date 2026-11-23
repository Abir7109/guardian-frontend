# Testing, QA & Visual Parity

The Kotlin project's test story is broken: `GreetingScreenshotTest` references a `Greeting()` composable that does not exist (compile error), `ExampleRobolectricTest` asserts `"My Application"` against a string that says `"Guardian"`, and `ExampleInstrumentedTest` asserts the wrong package name. **None of the three can pass.** The Flutter port must not inherit that — tests are the only way to prove a rewrite is faithful.

## 1. Test pyramid for this port

```
        ▲  Manual parity review (device, side-by-side)      ~2h/sprint
       ╱ ╲
      ╱   ╲  Golden / screenshot tests  (alchemist)          1 per screen + 6 painters
     ╱─────╲
    ╱       ╲ Widget tests (behaviour, semantics, states)   ~40
   ╱─────────╲
  ╱           ╲ Unit tests (services, hashing, dash math)   ~60
 ╱─────────────╲
```

## 2. Unit tests (pure Dart, fast)

| Target | Cases |
|---|---|
| `dashPath` | segment count, total length ≤ source, phase offset, closed circle, hexagon perimeter — **already written in `test/dash_path_test.dart`** |
| `PinService` | correct PIN, wrong PIN increments, hash is salted (same PIN + different salt ⇒ different hash), 5th attempt locks, lockout expiry |
| `ArmedState` logic | offline + geofence exit ⇒ correct state precedence |
| `GeofenceService` | inside/outside transition fires once, not per GPS tick (debounce) |
| `AuditRepository` | insert/query ordering, relative-time formatting |
| `AuditLog` retention | caps at N rows, oldest evicted |
| Form validators | email regex table-driven, password policy |
| `AdvisorPromptBuilder` | context is summarised, no secrets included, N events max |

## 3. Widget tests

| Screen | Assertions |
|---|---|
| Splash | shows logo + version from `PackageInfo`; routes on boot future completion; no infinite pending timer (`pumpAndSettle` succeeds) |
| Onboarding | 3 pages; swipe advances; Continue advances; Skip hidden on page 3; `seenOnboarding` written |
| Login | empty submit → validation errors; bad creds → error banner with `liveRegion`; submit disables fields; CTA shows spinner |
| Permissions | denied → shows "OPEN SETTINGS"; granted → advances; skip never marks granted; resumable at correct step |
| Home | tap core → `onLockdown`; stats render `—` when null; greeting matches hour; bell shows badge only when unread > 0 |
| Profile | 2FA label reflects state (**regression test for the Kotlin bug**); toggle persists via provider; disconnect shows confirm |
| Lockdown | 6 digits required; wrong PIN shakes + clears + error visible ≥1.2 s; lockout countdown blocks keys; `PopScope` prevents back; biometrics path calls verifier |
| Nav | selected tab semantics; routes correct; ALERTS tab is live (Kotlin's was dead) |

## 4. Golden / screenshot tests

Use `alchemist` (deterministic, no font fallback surprises) rather than raw `matchesGoldenFile`.

**Coverage target — one golden per painted surface, at 2 device sizes:**

| Golden | Sizes |
|---|---|
| `splash` | 390×844, 320×640 |
| `onboarding_p1/p2/p3` | 390×844 |
| `login` (idle, error, loading) | 390×844 |
| `permissions_stepN` (n=1..6) | 390×844 |
| `home` (armed, lockdown, offline) | 390×844, 600×960 |
| `profile` | 390×844 |
| `lockdown` (idle, error, success, lockout) | 390×844, 320×640 |
| `painter_radar` | standalone 240², angles fixed |
| `painter_safezone` | standalone 320×160, pulse 0/.5 |
| `painter_orbit` | standalone 220² |
| `painter_avatar` | standalone 90² |

**Rules:** freeze all animations to a fixed `t` (pass explicit values, never wall-clock); pin the font; generate on CI (Linux) only — never commit goldens generated on macOS.

## 5. Visual parity method (Kotlin vs Flutter)

The only trustworthy check for "did the port look the same" is side-by-side on the same hardware.

1. Build the Kotlin APK at `fd0e6d6` (needs a `debug.keystore` — see `07-RISKS-AND-DECISIONS.md R1`).
2. Install both on one Pixel 8.
3. For each of the 7 screens, screenshot both at the same brightness/scale.
4. Overlay at 50 % in Figma/Photoshop and measure deviation on: primary button bounds, card radii, text baselines, icon centres, nav height.
5. **Pass bar:** ≤2 dp deviation on primary elements, ≤1 sp on the 12 sp labels, exact hex match on all flat fills.

Automate steps 3–4 with `flutter drive` + `screenshot` package and an emulator running the Kotlin build, if the budget allows.

## 6. Device matrix

| Tier | Device | Why |
|---|---|---|
| Low | 2 GB RAM, Android 10, 320 dp wide | min supported; catches overflow + jank |
| Mid | Pixel 6a, Android 14 | realistic performance target |
| High | Pixel 8 Pro, Android 16 | latest API behaviour |
| Tablet | 600 dp+ | confirms layout doesn't break |
| iOS | iPhone SE (small) + 15 Pro | safe-area, notch, gesture-bar |
| A11y | TalkBack on, 200 % text scale, animations off | must remain usable |

**Specific things that will break at 320 dp:** the 288 dp keypad + 24 dp padding (fits, barely — verify), the 3 stat cards (text may clip → use `FittedBox` or reduce caption size), "UPDATE SAFE LOCATION" label, and the splash `GUARDIAN` at 8 sp letter-spacing (may overflow — constrain).

## 7. Performance gates

- [ ] 60 fps sustained on Home with all animations (DevTools frame chart: no frame > 16 ms in a 30 s capture)
- [ ] `--profile` build: no `saveLayer` from unnecessary `Opacity`/`ClipRRect`; prefer `withValues(alpha:)` on colour over `Opacity` widget
- [ ] No `shader` recreation per frame — cache `SweepGradient.createShader` in the painter (already done in `AvatarSweepPainter`, but verify)
- [ ] Cold start < 1.5 s to first frame on mid-tier
- [ ] Memory: no leak from the `Timer.periodic` in Lockdown (test with `dispose` assertion)

## 8. Accessibility gates

- [ ] Every interactive element has a semantic label; verified with TalkBack walkthrough
- [ ] Contrast: `outline #8D90A1` on `slate #131316` ≈ 4.6:1 ✅; `onSurfaceVariant #C3C5D8` on `voidBlack` ✅; **check** `outlineVariant #434655` used as text anywhere (it shouldn't be — it's a border colour)
- [ ] Text scales to 200 % with no clipping on all 7 screens
- [ ] Tap targets ≥ 48 dp — audit the 40 dp bell and 20 dp icons
- [ ] All animations respect `MediaQuery.disableAnimations`
- [ ] Lockdown screen announces digit count changes (live region)
- [ ] No colour-only information (the armed state must also differ in text, which it does)

## 9. Release checklist

- [ ] `flutter analyze` → 0 issues
- [ ] `dart format --set-exit-if-changed .`
- [ ] All goldens pass on CI
- [ ] Proguard/R8 rules for Firebase + drift (`flutter build appbundle --release` tested)
- [ ] Play Console: data-safety form matches reality (location, camera, device IDs)
- [ ] Background-location justification video recorded (required for review)
- [ ] Crashlytics/analytics wired and consent-gated
- [ ] Version from `pubspec.yaml` → also surfaced on Splash (kills the hardcoded `v2.4.0`)

# Risks, Gotchas & Open Decisions

## A. Migration risks (ranked by likelihood × impact)

| # | Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|---|
| **R1** | **You cannot build the Kotlin app to compare against.** It has no Gradle wrapper and its `debugConfig` signing points at a gitignored `debug.keystore`. | Certain | High — no parity baseline | Before touching Flutter: create the keystore (`keytool -genkey -alias androiddebugkey -storepass android -keypass android -dname "CN=Android Debug,O=Android,C=US" -keyalg RSA -keysize 2048 -validity 10000 -keystore debug.keystore`) **or** delete that `signingConfig` line. Then capture reference screenshots for all 7 screens once, and commit them to `docs/reference/`. |
| **R2** | **Dash rendering mismatch.** Flutter has no `PathEffect`; hand-rolled dash math can produce visibly different ring density. | High | Medium — visible, feels wrong | `dashPath` is written + unit-tested here. Convert dp→px with `devicePixelRatio` (Kotlin's `toPx()` did this implicitly). Golden-test each painter at 2 densities. |
| **R3** | **Flutter 3.47 Material decoupling.** `package:flutter/material.dart` is deprecated in the Nov 2026 stable; Material is moving to `material_ui`. | Certain (this month) | Medium — mechanical churn | Confine Material imports to `theme/` + `core/widgets/`. Run `dart fix --apply --code=migrate_design_widgets` once, early. Don't reference Material types in `data/`. |
| **R4** | **Background geofencing is the riskiest feature.** Android needs a foreground service + notification; iOS needs `always` authorisation and is aggressively throttled; Play requires a review video. | High | High — could block release | Build the *visual* Safe Zone first (S3), wire real geofencing last (S5). Have a graceful "approximate only" fallback that still shows last-known position. |
| **R5** | **Store rejection over permissions.** The Kotlin app declared none, so the wording was never tested. Play rejects vague rationales, especially background location and device admin. | Medium | High | Rewrite every rationale in plain language; ship device-admin **off by default and opt-in**; record the justification video. |
| **R6** | **"Military-grade", "SATS 12", "98% COVERAGE" are unverifiable claims.** Presenting fabricated telemetry is both a trust and (in the US) an FTC/advertising risk. | Certain if ported as-is | Medium | Every metric must be either real or shown as `—`. Soften the marketing copy. |
| **R7** | **256 character-flood of raw hex** if the port drifts from tokens. | Medium | Medium — design rot | Custom lint banning `Color(0x` outside `theme/`; the `05-DESIGN-SYSTEM.md` table is the authority. |
| **R8** | **Goldens are environment-sensitive.** Fonts differ by host OS; a golden generated on macOS fails on Linux CI. | High | Low (annoying) | Bundle fonts, generate goldens on Linux only, use `alchemist` with fixed text scale. |
| **R9** | **Firebase AI needs a real Firebase project** (`google-services.json` / `GoogleService-Info.plist`). The Kotlin project deliberately set `missingGoogleServicesStrategy = WARN`, i.e. it never had one. | Certain | Medium — S6 blocked | Create the project in S0, not S6. Enable App Check with reCAPTCHA/DeviceCheck before the first AI call. |
| **R10** | **Scope creep.** The Kotlin app is a demo; "recreate it" naturally expands into "make it real", which is 3× the work. | High | High | The phase plan in `00-MASTER-PLAN.md §6` separates *visual parity MVP* (S3) from *real product* (S7). Get sign-off on which one you're buying. |
| **R11** | Package name. Kotlin uses `com.aistudio.guardian.tactical` — tied to AI Studio and to an app ID you won't control long-term. | High | High if changed late | Decide **before** first store upload. Changing later = new listing, lost installs/reviews. |
| **R12** | Legal: remote wipe / device admin on someone else's phone can be abused (stalkerware pattern). | Low | Severe | Never ship wipe without re-auth + confirmation + delay. Publish a clear policy. Consider dropping wipe entirely for v1. |

## B. Compose→Flutter traps that will actually bite you

1. **`Modifier.rotate()` takes degrees; `Transform.rotate()` takes radians.** Every angle from the Kotlin source must be converted (`deg * pi / 180`). `RadarPainter` already receives radians.
2. **`Modifier.offset()` is paint-only in both frameworks** — so absolute-position assumptions carry over, but hit-testing follows the layout box, not the visual position. This matters for the "SIMULATE BREACH" pill overlay.
3. **`Arrangement.spacedBy()` has no Flutter equivalent.** Missing gaps are the #1 cause of a port looking "almost right".
4. **`RadialGradient.radius` is a fraction in Flutter, pixels in Compose.** The glow discs in the Kotlin source are 300/400/450 *px*; in Flutter you need `radius: 0.5`-ish or a sized `DecoratedBox`.
5. **`BoxShadow` has one colour** vs Compose's separate `ambientColor`/`spotColor`. Pick the glow colour; ignore the other.
6. **`Card` + `BorderStroke`** → `Card(shape: RoundedRectangleBorder(side: BorderSide(...)))`. A `Container` with a `border` decoration is usually simpler.
7. **Nested scroll:** Compose allowed a `verticalScroll` `Column` containing `LazyColumn`. Flutter throws on unbounded nested scrollables — use a single `CustomScrollView` with slivers (`HomeScreen` already does).
8. **`remember` does not survive process death, and neither does Flutter's `State`** — both need explicit persistence. The port adds `flutter_secure_storage`.
9. **`LaunchedEffect(Unit)` is not `initState`.** `initState` runs before layout; use `addPostFrameCallback` for anything needing size, and always cancel timers in `dispose`.
10. **`Icons.Default.X` → `Icons.x`** but not 1:1: no Material icon for the bespoke HUD glyphs. Budget `flutter_svg` for 3–5 custom icons (shield variant, hexagon badge, policy scale).

## C. Open decisions (blocking)

| # | Question | Why it blocks | Recommendation |
|---|---|---|---|
| D1 | **Backend?** none / Firebase / custom API | Determines S5 entirely (auth, sessions, remote lock, cross-device) | Firebase (Auth + Firestore + FCM + AI) — matches the deps the Kotlin project already imported |
| D2 | **Remote wipe + device admin in v1?** | Legal/review risk (R12), Play justification video | **Defer to v2.** Ship lock + locate + notify + alarm |
| D3 | **iOS in scope for v1?** | Background geofence and intruder-camera are constrained on iOS; doubles QA matrix | Android-first, iOS at S7 with a reduced feature set (no remote wipe, foreground-only geofence) |
| D4 | **Keep the exact brand or refresh?** | All assets here assume "keep" | Keep — the HUD identity is the product's only real differentiator |
| D5 | **Package / bundle ID?** | Irreversible after first upload (R11) | `net.guardian.app` (or your own domain) — **not** `com.aistudio.*` |
| D6 | **PIN length 4 or 6?** | Changes keypad layout + spec | 6. 4 digits with a 5-attempt limit is weak for a device-lock use case |
| D7 | **Monetisation?** | The UI already shows a "PRO" badge and "premium" copy | Decide before S7; badge must reflect real entitlement, never be decorative |
| D8 | **Analytics/crash reporting + consent?** | Data-safety form accuracy | Crashlytics + opt-in analytics, GDPR/CCPA consent screen |

## D. What I'd do differently from the Kotlin app, in one paragraph

The Kotlin app optimises for *looking* operational: every metric is a confident number, every toggle appears wired, every screen says something authoritative. That's great for a pitch and actively harmful for a security product, where a false "SYSTEM ARMED" is worse than no indicator at all. The Flutter port should keep 100 % of the aesthetic and invert the honesty: show `—` when unknown, show the real armed state, make every toggle persist, make the PIN real, and let the AI advisor explain events instead of inventing them. That single principle — **the HUD must never lie** — is the porting spec in one line.

# Guardian — Kotlin/Compose → Flutter Port: Master Plan

> **Source:** `Abir7109/guardian-frontend` @ `fd0e6d6` — Android, Kotlin 2.2.10, Compose BOM 2024.09.00, one 2,740-line `MainActivity.kt`.
> **Target:** Flutter 3.47+ / Dart 3.12+, Android + iOS, one codebase.
> **Verdict on the brief:** you are right — this **is** a rewrite, not a conversion. ~100% of the UI is Compose (no XML layouts), there is no state layer, no backend, and no data layer to port. What you *do* carry across is the **design system, screen inventory, interaction spec, and copy**. Everything else is rebuilt.

---

## 1. What "porting" actually means here

There is no automated Compose→Flutter transpiler, and there never will be a good one: Compose is a Kotlin-inline-scope DSL with a modifier chain, Flutter is a widget-tree with explicit composition objects. The two are *conceptually* close (both declarative, both immutable rebuild-on-state-change) but **not** syntactically mappable. So the port is done as:

| Phase | Task | Ratio of effort |
|---|---|---|
| 1 | **Spec extraction** — freeze the Kotlin app as the reference: colours, type scale, spacing, screen states, copy, timings | 10% |
| 2 | **Design system in Flutter** — tokens, theme, shared widgets, custom painters | 20% |
| 3 | **Screen-by-screen rebuild** — 7 screens + nav shell | 40% |
| 4 | **State + persistence** — replace `remember { mutableStateOf }` with Riverpod + secure storage | 15% |
| 5 | **Real features** — auth, permissions, geofence, biometrics, Gemini | 15% |
| 6 | **Parity QA** — golden tests + side-by-side device comparison | continuous |

**Rule for the port:** the Kotlin app is a *visual spec, not a code spec*. Where its behaviour is fake (the `1234` PIN, the accept-anything login, the no-op permission screen), **do not port the fake behaviour** — port the *intent* and implement it properly. See `01-FEATURE-RESEARCH.md`.

---

## 2. What is being rebuilt (inventory)

**Screens (7):** Splash · Onboarding · Login · Permissions · Home · Profile · Lockdown overlay. Plus a persistent bottom-nav shell (Home / Features / Profile) and a floating "simulate breach" dev affordance (→ dev-only, see 5.3).

**Custom-drawn graphics (the hard part — must be re-authored as `CustomPainter`):**
1. Splash radar: two concentric circles + radial glow + 2 shimmer progress bars.
2. Onboarding orbital: hexagon path + 220 dp rotating container with 3 satellite chips.
3. Home radar HUD: 2 counter-rotating dashed rings + pulsing scale + 130 dp glowing core button.
4. Safe Zone map: 2 crosshair lines + dashed geofence circle + center dot + expanding pulse ring + pin.
5. Lockdown: 160 dp orbit rings + 80 dp shield with colored shadow, heartbeat radial glow, 4 PIN dots, spring shake.
6. Profile avatar: 360° sweep-gradient ring rotating at 4 s/rev.

**Reusable components (7):** `SettingsSectionHeader`, `SettingsToggleRow`, `SettingsValueRow`, `LogItemRow`, `GuardianBottomNavBar`, `NavBarItem`, gradient primary button + outlined ghost button.

**Assets that must be added:** a real font (the Kotlin app has none), the logo, avatars, map backdrop. The 3 remote `lh3.googleusercontent.com` URLs in the Kotlin app are **not** stable — they must be replaced by bundled assets (provided in `assets/`).

---

## 3. Flutter stack decision

| Concern | Choice | Why |
|---|---|---|
| State | **Riverpod 3** (`flutter_riverpod` + `riverpod_annotation`) | Compile-safe DI, no BuildContext coupling, trivially testable. Kotlin app's hoisted-`remember` state maps 1:1 onto providers. |
| Navigation | **go_router 16** | Kotlin uses a hand-rolled `enum GuardianScreen` + `AnimatedContent`. go_router gives typed routes, deep links (`/lockdown` from a push notification), and a redirect hook for auth gating — which the enum switch cannot do. |
| Animation | **flutter_animate** + core `AnimationController` | Replaces `rememberInfiniteTransition` / `animateFloat` / `spring`. |
| Secure storage | **flutter_secure_storage** (Keystore/Keychain) | Replaces the hardcoded PIN. Never store a PIN in SharedPreferences. |
| Biometrics | **local_auth 3** | `BiometricPrompt` on Android, `LAContext` on iOS. |
| Location/geofence | **geolocator** + native geofencing (`flutter_background_geolocation` or platform channels) | Powers the real "Safe Zone". |
| Device integrity | **device_info_plus**, **flutter_jailbreak_detection** | SIM/root/jailbreak signals for the threat engine. |
| HTTP | **dio** | Replaces Retrofit; interceptors for auth + logging (Kotlin already had OkHttp logging interceptor but never used it). |
| Local DB | **drift** | Replaces unused Room. Logs/events/geofence history. |
| AI | **firebase_ai** | Direct successor to the `firebase-ai` Gradle dep the Kotlin app declared but never used. |
| Images | **cached_network_image** | Replaces Coil. |
| Fonts | **google_fonts** or bundled TTFs | The Kotlin app has zero custom type. Bundled preferred for offline. |
| Tests | `flutter_test`, **alchemist**/**golden_toolkit**, `mocktail` | Replaces Roborazzi. Kotlin's screenshot test doesn't even compile — don't repeat that. |
| Lints | `flutter_lints` + **very_good_analysis** | — |

> ⚠️ **Timing note (important, this month):** Flutter 3.47 moved Material and Cupertino out of the SDK into the standalone `material_ui` / `cupertino_ui` packages, and `package:flutter/material.dart` is **formally deprecated in the November 2026 stable**. Start the port importing `package:flutter/material.dart` (still works), but structure `lib/theme` so the swap is one mechanical `dart fix --apply --code=migrate_design_widgets` run. Do not let Material widget types leak into business logic.

---

## 4. Project structure (target)

```
lib/
├─ main.dart
├─ app.dart                     # MaterialApp.router, theme, observers
├─ router/
│  ├─ app_router.dart            # GoRouter + auth redirect
│  └─ routes.dart                # typed route constants
├─ theme/
│  ├─ colors.dart                # GuardianColors — 1:1 from Color.kt
│  ├─ gradients.dart
│  ├─ typography.dart
│  ├─ spacing.dart               # 4pt scale + radius tokens
│  ├─ motion.dart                # durations + curves (from Compose timings)
│  └─ guardian_theme.dart        # ThemeData(dark) composition
├─ core/
│  ├─ widgets/                   # GlassCard, HudSurface, PrimaryGradientButton,
│  │                             # GhostButton, SettingsRow, SectionHeader, LogTile,
│  │                             # GuardianBottomNav, HudChip, ScanlineOverlay
│  ├─ painters/                  # RadarPainter, OrbitalPainter, HexagonPainter,
│  │                             # SafeZoneMapPainter, AvatarSweepPainter, DashedCircle
│  ├─ utils/                     # dash path helper, haptics, formatters
│  └─ extensions/
├─ features/
│  ├─ splash/                    # screen + controller
│  ├─ onboarding/                # pageview (3 pages — see 5.1)
│  ├─ auth/                      # login, signup, google sign-in, session
│  ├─ permissions/               # 6-step wizard (see 5.2)
│  ├─ home/                      # HUD dashboard
│  ├─ safe_zone/                 # map + geofence editor
│  ├─ profile/                   # operator card, security protocols, logs
│  ├─ lockdown/                  # PIN overlay + pin service
│  ├─ alerts/                    # breach feed (new)
│  └─ advisor/                   # Gemini threat advisor (new)
└─ data/
   ├─ models/                    # freezed + json_serializable
   ├─ repositories/
   └─ services/                  # api, secure_store, location, biometric, ai
```

**Layering rule:** `features/*` may import `theme`, `core`, `data`. `core` may import `theme` only. `data` imports nothing from `features`. This is the boundary the Kotlin file is missing entirely.

---

## 5. Deliberate deviations from the Kotlin app

These are **fixes, not porting gaps**. Sign off on each.

### 5.1 Onboarding: 1 page → 3 pages
Kotlin shows "01 / 03" and renders 3 pagination dots but only ships one page — Skip and Continue both jump to Login. **Rebuild as a real `PageView` with 3 pages:**
1. *Defense That Never Sleeps* — orbital illustration, "Premium military-grade protection for your mobile lifestyle." (existing copy)
2. *Know The Moment It Moves* — geofence/safe-zone illustration, real-time movement + tamper alerts.
3. *Recover, Don't Just Lose* — biometric + AI advisor, "Your device reports back; you decide what happens next."

### 5.2 Permissions: 1 screen claiming 6 steps → real wizard
Kotlin says "Step 2 of 6" and "GRANT ACCESS" but requests **zero** runtime permissions (Accompanist is commented out; the manifest declares none). Rebuild as 6 steps with `permission_handler`, each with a plain-language rationale (Play Store requirement) and a graceful "denied / permanently denied → open settings" path:

| Step | Permission | Purpose |
|---|---|---|
| 1 | Notifications | Breach + movement alerts |
| 2 | Location (while-in-use) | Safe zone / geofence |
| 3 | Background location | Geofence while app closed — **justify hard**, Play requires a video for review |
| 4 | Camera | Intruder snapshot on failed unlock |
| 5 | Biometrics | Gate sensitive actions |
| 6 | Device admin *(optional)* | Remote lock — **opt-in only**, never bundle silently |

### 5.3 "SIMULATE BREACH" pill
Currently shipped in the production UI at `top = 80.dp` over Home and Profile. Move behind `kDebugMode` / a hidden long-press on the version label.

### 5.4 PIN `1234` → user-set PIN
Kotlin hardcodes `if (pinCode == "1234")`. Rebuild: 6-digit PIN set at onboarding, stored as a **salted hash in secure storage**, with attempt counter, exponential lockout, and biometric fallback. See `04-SCREEN-BY-SCREEN-REBUILD.md §7`.

### 5.5 2FA row bug
Profile shows a hardcoded `"OFF"` string while the toggle mutates real state — the UI never updates. Trivial fix, called out so it isn't copied into Flutter.

### 5.6 Dead nav tab
`FEATURES` in the bottom bar is `onClick = { }`. Rebuild it as the **Alerts/Features** tab so the nav has 3 real destinations.

---

## 6. Phased delivery

Assume 1 Flutter dev + part-time designer. Adjust linearly.

| Sprint | Duration | Deliverable | Exit criteria |
|---|---|---|---|
| **S0 — Foundations** | 3 d | Repo scaffold, lints, CI, theme tokens, `GuardianColors`, typography, spacing, motion constants | `flutter analyze` clean; theme demo screen matches Kotlin `Color.kt` swatches |
| **S1 — Shell + nav** | 4 d | `MaterialApp.router`, go_router tree, bottom nav, AnimatedSwitcher transitions | Can walk Splash→Onboarding→Login→Home→Profile with placeholder bodies, transitions feel like 400 ms crossfade |
| **S2 — Custom painters** | 5 d | `RadarPainter`, `DashedCircle` helper, `OrbitalPainter`, `HexagonPainter`, `SafeZoneMapPainter`, `AvatarSweepPainter` | Golden tests for each painter; dashes match Kotlin `dashPathEffect` intervals |
| **S3 — Static screens** | 6 d | Splash, Onboarding (3 pages), Login, Permissions (6 steps), Home, Profile, Lockdown — pixel-focused | Side-by-side with Kotlin APK on a Pixel 8 at <2 dp deviation on primary elements |
| **S4 — State layer** | 4 d | Riverpod providers, secure store, session, settings persistence, PIN service | Kill app → state survives; PIN survives; toggles survive |
| **S5 — Real integrations** | 8 d | Auth (email + Google), permission flows, geofencing, biometrics, local notifications, intruder capture | All 6 permission steps functional on device; geofence fires a notification |
| **S6 — AI advisor** | 5 d | `firebase_ai` Gemini chat, App Check, prompt guardrails, streaming UI | Ask "why did I get an alert at 3am" → coherent, grounded answer; no key on client |
| **S7 — Hardening + ship** | 6 d | a11y pass, i18n, CI goldens, Play/iOS store metadata, crash reporting | Zero analyzer warnings, ≥80% coverage on `data/`+`core/`, store-ready |

**Total ≈ 41 dev-days** to a shippable v1. A **visual-parity MVP** (screens working with fake data, no backend) lands at end of **S3 ≈ 18 days**.

---

## 7. Definition of Done (per screen)

- [ ] Matches Kotlin reference at ≥3 device sizes (small phone 320 dp, Pixel 8, tablet 600 dp)
- [ ] No hardcoded colours — all via `GuardianColors` / `Theme.of(context)`
- [ ] No hardcoded strings — all via `AppStrings` (i18n-ready)
- [ ] All `Semantics` / `contentDescription` present; contrast ≥4.5:1; tap targets ≥48 dp
- [ ] Light + dark (Guardian is dark-only by brand, so: dark-only + a forced-light regression test that asserts graceful degradation)
- [ ] Golden test committed
- [ ] Handles empty / loading / error states
- [ ] 60 fps on a mid-range Android device (DevTools frame chart, no jank on animations)
- [ ] `flutter analyze` clean, no `// ignore:` without justification

---

## 8. Decisions I need from you

1. **Backend?** The Kotlin app has none. Do we (a) stay local-only + Firebase AI, (b) Firebase (Auth/Firestore/FCM), or (c) custom API? This changes S5 substantially.
2. **Remote-wipe / device-admin?** Legally and reputationally sensitive; Play requires justification and it's a common reason for rejection. In for v1 or defer?
3. **iOS?** The brief says "Flutter" — cross-platform is the whole point, but background geofencing + intruder camera on iOS have hard OS limits. Ship Android-first, iOS at S7?
4. **Brand:** keep the exact Kotlin palette/wordmark, or should the Flutter app get a refreshed identity? All assets here assume **keep**.
5. **Package name:** keep `com.aistudio.guardian.tactical`, or move to a proper reverse-domain (e.g. `net.guardian.app`)? Changing after store release is painful — decide now.

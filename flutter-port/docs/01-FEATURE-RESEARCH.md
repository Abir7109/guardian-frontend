# Feature Research: What Guardian Is, and What It Should Be

## 1. Purpose — read from the code, not the marketing

`metadata.json` calls Guardian a *"Tactical anti-theft intelligence suite with real-time HUD metrics, biometric lock systems, and custom lockdown protocol visualizers."* That's the pitch. The **implemented reality** is narrower and clearer:

| Claim | Implemented? | Evidence in code |
|---|---|---|
| Real-time HUD metrics | ❌ Fabricated | `"12" SATS`, `"0" ALERTS`, `"98%" COVERAGE` are string literals |
| Biometric lock | ❌ Missing | `biometricEnabled` is a toggle that does nothing; no `BiometricPrompt` |
| Custom lockdown protocol | ✅ Visually | `AlarmOverlayScreen`, 560 lines, real animation, but PIN is `"1234"` |
| Anti-theft | ⚠️ Themed only | No location, no SIM monitoring, no remote commands |
| AI (Gemini) | ❌ Missing | `firebase-ai` in Gradle, `GEMINI_API_KEY` in `.env`, zero call sites |
| Safe zone | ❌ Fake | Static image + canvas crosshair; "UPDATE SAFE LOCATION" is a no-op |
| Login | ❌ Fake | Both SIGN IN and Google call `onLoginSuccess` directly |

**So the true purpose of the current repo is: a design prototype and pitch deck.** Its value to the port is the **visual language and information architecture**, both of which are genuinely good (see §4).

The genuine product intent — reconstructed from copy, icons and structure — is:

> A premium, tactical-styled mobile security app that makes device protection **legible and controllable**: the owner sees at a glance that the device is armed, where it is relative to a trusted zone, who accessed it and when, and can force a lockdown from the device itself or remotely. The "tactical HUD" aesthetic is the differentiator against grey, corporate security apps.

That intent is achievable, and §3 maps it to real platform capabilities.

---

## 2. Market research — what an anti-theft app must actually do

Competitive scan of the category (Bitdefender Anti-Theft, Prey, Cerberus, Where's My Droid, CrookCatcher, plus the OS vendors' own features) converges on a **five-stage model**. Users expect coverage at every stage, not one big "PROTECT" switch:

| Stage | Capability | Notes from the field |
|---|---|---|
| **Prevent** | App lock, screen-lock hygiene, PIN/password change guard, "no unlock without credentials" | Bitdefender and Cerberus both gate *their own settings* behind the app PIN — otherwise a thief just turns protection off |
| **Detect** | Wrong-PIN attempts, SIM swap/removal, motion/snatch detection, offline detection, failed face-match | OS vendors now do this natively: Android 15's **Theft Detection Lock** (AI on motion patterns), **Offline Device Lock**, and **Remote Lock**; Google's **Identity Check** demands biometrics for sensitive changes away from trusted locations |
| **Alert** | Push + SMS to owner and a trusted contact, with photo of the intruder | CrookCatcher/LookWatch are built entirely on "snap a mugshot on failed unlock" — it's the single most-loved feature in reviews |
| **Locate** | Live GPS, location history trail, last-known before battery death, geofence enter/exit | Prey and Cerberus both keep a breadcrumb trail, not just a single pin |
| **Respond** | Remote lock, remote wipe, siren/scream alarm, on-screen message to the finder | "Screaming alarm" is a reliable deterrent; remote **wipe** is increasingly restricted (Bitdefender's wipe is Android ≤13 only) because of enterprise/MDM conflicts |

**Two hard lessons for Guardian's roadmap:**

1. **Don't rebuild the OS.** Android 15+ and iOS 18+ already ship theft detection, offline lock and remote lock. A third-party app that duplicates those will be seen as bloat and will be rejected/ignored. Guardian's differentiation must be:
   - **Cross-platform + cross-device** visibility (the OS only protects its own device),
   - **Breadcrumb history and forensic log** (the OS gives you one pin, not a trail),
   - **The tactical UX** — a legible "armed/locked/breached" state machine instead of buried settings,
   - **Sensor fusion into one timeline**: unlock attempts + geofence + SIM + app foreground changes in one feed,
   - **The AI advisor** — turning raw events into "here's what happened and what to do".
2. **Wipe is the most dangerous feature.** Irreversible, abusable (a stolen phone that gets wiped is also unrecoverable), and a Play-policy minefield. Recommend a **two-step** design: "Lock + notify" is one tap; "Erase" requires re-authentication, a typed confirmation phrase, and a 24-hour delayed-execution window you can cancel.

### 2.1 Where the Kotlin app's screens map onto this model

| Kotlin screen | Stage | Verdict |
|---|---|---|
| `SplashScreen` | — | Brand moment. Keep, but shorten to ≤1.2 s (2.5 s is a churn risk) |
| `OnboardingScreen` | Prevent (education) | Keep, expand to 3 pages |
| `LoginScreen` | Prevent (access) | Rebuild with real auth |
| `PermissionsScreen` | All (enabler) | Rebuild as a truthful 6-step wizard |
| `HomeScreen` | Detect + Locate | Keep layout, make metrics real |
| `ProfileScreen` | Prevent (config) + Alert history | Keep, add real audit log |
| `AlarmOverlayScreen` | Respond | Keep visual, make PIN real + add "notify" action |
| *(missing)* | Locate/Alert | **New:** Alerts feed, movement timeline |
| *(missing)* | Respond (remote) | **New:** remote-command surface (deferred — see decisions) |

---

## 3. Feature rebuild spec — each feature, its Flutter plan

### F1 · Splash & brand moment
**Kotlin:** `LaunchedEffect { delay(2500); onTimeout() }`, radial glow, hexagon path, two shimmer bars, "v2.4.0".
**Flutter:** `SplashScreen` with a single `AnimationController` (2800 ms) driving all three sub-animations via `Interval` curves — one controller beats six `rememberInfiniteTransition`s for frame budget. Gate on `Future.wait([firebaseInit, secureStore.read, minimumDelay(900ms)])` so you never show a fake delay.
**Timings to copy:** shimmer line 1500 ms linear loop; glow radius 300; version label bottom padding 32.

### F2 · Onboarding
**Kotlin:** one static page, `Angle` anim 12 000 ms linear for the orbit, 3 dots, skip/continue → Login.
**Flutter:** `PageView` + `PageController`; `OrbitalPainter` fed by a repeating `AnimationController` (12 s, `Curves.linear`). Persist "seen" in secure store → subsequent launches skip to auth.
**Acceptance:** swipe + button both advance, dots animate width 5→24 with `AnimatedContainer`, "Skip" only on pages 1–2.

### F3 · Authentication
**Kotlin:** accepts any input; pre-filled fake credentials.
**Flutter:** `AuthRepository` interface with two impls — `FirebaseAuthRepository` (production) and `FakeAuthRepository` (tests/demo). Email+password with real validation (`Form` + `TextFormField.validator`), Google Sign-In via `google_sign_in` + Firebase credential exchange, and a **biometric quick-unlock** path for returning sessions (`local_auth`). Never pre-fill credentials — that's a security smell as much as a UX one.
**States:** idle / submitting / error(inline, `Semantics(liveRegion)`) / success. Rate-limit to 5 attempts before cooldown.

### F4 · Permission wizard
**Kotlin:** 6-step rail visual, 0 requests.
**Flutter:** `permission_handler`; one page per permission; a shared `PermissionStepController` that reports `granted | denied | permanentlyDenied | restricted`. `permanentlyDenied` → `openAppSettings()` CTA. "Skip for now" always available except background location (degrade accordingly). Track completion so the flow is resumable.
**Platform note:** background location requires a foreground service + a persistent notification on Android, and on iOS requires `NSLocationAlwaysAndWhenInUseUsageDescription` plus a review justification. Budget for store review here.

### F5 · Home HUD (the flagship)
**Kotlin:** static avatar/bell, radar with core "SYSTEM ARMED" button, 3 stat cards, Safe Zone card with map + crosshairs + pulse + pin.
**Flutter:** `CustomScrollView` mirroring the `Column` + `verticalScroll`; `RadarPainter` (2 dashed rings, counter-rotating 24 s / 16 s, `glowScale` 0.95↔1.05 2 s `easeInOutSine`), core is a `GestureDetector`→`InkWell` over a 130 dp circle with `BoxShadow` replacing Compose's ambient/spot glow. Stats come from a `homeStatsProvider` (StreamProvider) so they can be wired to real data later without touching layout.
**"SYSTEM ARMED" button** must reflect a real state machine: `armed | disarmed | lockdown | offline`. The Kotlin version always says ARMED, which is a lie if the app has no protection — fix.
**Safe Zone:** replace the remote `lh3.googleusercontent.com` backdrop with bundled `assets/illustrations/safe_zone_map.png` (offline-safe, no third-party URL) or a live `flutter_map`/Google Maps tile layer once F8 lands.

### F6 · Operator profile & security protocols
**Kotlin:** rotating sweep-gradient avatar, PRO/VERIFIED badges, biometric switch (real state), 2FA row (**bug:** label stuck on "OFF"), active sessions, alert config, region, 2 recent logs, DISCONNECT.
**Flutter:** `SettingsRow` widget with variants `toggle | value | destructive`; toggles bound to `settingsProvider` (persisted); 2FA value derived: `twoFactorEnabled ? 'ON' : 'OFF'`; logs from `auditLogProvider` (drift table) with relative timestamps.
**New:** "Active Sessions" should open a real list (device, last seen, revoke) — that's a genuine security feature and it's already promised by the UI.

### F7 · Lockdown overlay
**Kotlin:** full-screen red heartbeat, marquee bar + `HH:mm:ss` ticker, 160 dp shield, 4-dot PIN, keypad with letter sub-labels, spring shake on wrong, success → "GRID ACCESSIBLE".
**Flutter:**
- `TickerMode`-safe clock: one `Timer.periodic(1s)` inside a provider, not a widget rebuild loop.
- Heartbeat: `AnimationController` 1500 ms `repeat(reverse: true)` with `Curves.easeInOutSine`, alpha 0.05→0.20, radial gradient via `RadialGradient`.
- Shake: sequence of `AnimationController.animateTo` or a `ShakeCurve`; copy the Kotlin shape — out to +20, back to −20, settle 0, `DampingRatioHighBouncy`, `StiffnessMedium`.
- PIN: **6 digits** (up from 4), hashed+salted in secure storage, 5 attempts → 30 s lockout doubling to a max, then biometric fallback. Optional: capture a front-camera still after 3 failures (needs camera permission — hence F4 step 4) and attach it to the audit log locally.
- This screen must be reachable as a **route** (`/lockdown`) so a push notification or an `AppLifecycleState` watchdog can raise it without a widget tree reference.

### F8 · Safe Zone / geofencing (new, promised by UI)
`geolocator` stream → `GeofenceService` with a circular zone (centre, radius). Enter/exit transitions push an event into `auditLog` + a local notification. Map: `flutter_map` + OSM tiles (no API key) or Google Maps if you want the visual match. Draw the geofence with the **same** `SafeZoneMapPainter` primitives so the empty/loading state matches the final state.

### F9 · AI Threat Advisor (new, the reason `firebase-ai` was in the Gradle file)
`firebase_ai` → `FirebaseAI.googleAI().generativeModel('gemini-2.5-flash')`. Feed it **structured event context**, not free text: last N audit rows, current armed state, geofence config. System instruction must scope it to security triage and forbid inventing device facts. Stream tokens into the chat UI. Gate behind App Check (the Kotlin project already had `firebase-appcheck-recaptcha`).
**Never** let the model trigger destructive actions — it advises, the human confirms.

### F10 · Alerts / Features tab (new, replaces the dead nav item)
A chronological feed merging geofence, unlock-attempt, SIM, permission and AI-insight events. Filters by severity. This one screen turns five disconnected features into a product.

---

## 4. What to preserve exactly (the good part)

These are the assets worth carrying over byte-for-byte in spirit:

- **Palette** — void black `#050507`, slate `#131316`, cyan `#49FCD9`, periwinkle `#B6C4FF`, alert red `#FF5167`, text `#E5E1E5`, muted `#C3C5D8`, borders `#434655`. This is a coherent, non-generic system (see `05-DESIGN-SYSTEM.md`).
- **Typography behaviour** — tiny letterspaced uppercase labels (9–12 sp, letterSpacing 1–2) as the "HUD voice" against large ExtraBold headings (26–32 sp). That contrast *is* the brand.
- **Motion language** — slow rotations (12–24 s), breathing scales (2 s), heartbeat glows (1.5 s), spring shakes. All linear/easeInOutSine, never bouncy except on error.
- **Information architecture** — the 4-card Home hierarchy (identity → armed state → metrics → location) is genuinely well-ordered; keep it.
- **Copy tone** — terse, procedural, second-person-free ("SYSTEM ARMED", "OPERATOR ACTIVE"). Keep, but humanise the permission rationales (see F4) for store review.

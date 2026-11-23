# Guardian — Repository Analysis Report

**Repo:** `Abir7109/guardian-frontend` · **Branch:** `arena/8c2ec473-guardian-frontend` (from `fd0e6d6` "Initial commit - Guardian frontend")
**Analysed:** all 47 tracked files (single commit history, clean working tree)

---

## 1. What this project actually is

Despite the name "guardian-**frontend**", this is **not** a web project. It is a **single-module native Android app** written in **Kotlin + Jetpack Compose (Material 3)**.

It was exported from **Google AI Studio** (`metadata.json` → `"name": "Guardian"`, `majorCapabilities: MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API`, README links an `ai.studio/apps/...` URL, and the package `com.aistudio.guardian.tactical`).

**Product concept:** a "tactical anti-theft intelligence suite" — a sci-fi HUD-styled security dashboard with real-time metrics, a biometric/2FA settings surface, and a full-screen device-lockdown PIN screen. Today it is a **high-fidelity UI prototype** — every screen is hand-built Compose, but almost nothing is wired to real data, auth, hardware, or the Gemini API.

---

## 2. Build & toolchain

| Item | Value |
|---|---|
| Gradle | Kotlin DSL, version catalog (`gradle/libs.versions.toml`) |
| AGP / Kotlin | 9.1.1 / 2.2.10 |
| SDK | compileSdk 36.1, targetSdk 36, **minSdk 24** |
| namespace / appId | `com.example` / `com.aistudio.guardian.tactical` |
| Compose BOM | 2024.09.00 |
| Tests | JUnit4, Robolectric 4.16.1, Roborazzi 1.59.0 (screenshot tests) |
| Plugins | android-application, kotlin-compose, KSP, Roborazzi, secrets, google-services |

**Dependencies actually enabled:** Compose (UI, Material3, icons-extended), lifecycle, activity-compose, Room runtime+ktx (+KSP compiler), Coil, Retrofit + Moshi converter, OkHttp + logging interceptor, Firebase AI (Gemini) + App Check reCAPTCHA, coroutines.

**Declared but commented out** (deliberately kept for later): Accompanist permissions, CameraX (camera2/core/lifecycle/view), DataStore, Navigation-Compose, Play Services Location, Firebase Auth + Firestore + Credentials/GoogleId.

**Secrets:** the `secrets` plugin reads `.env` / `.env.example` and is set up to expose `GEMINI_API_KEY` via `BuildConfig`. `.env` is gitignored.

### Build problems I found
1. **No Gradle wrapper** — there is no `gradlew`, no `gradle/wrapper/` at all. Builds are only reproducible by opening the project in Android Studio. Nothing can be built from CLI as-is (and this sandbox has no JDK/Gradle either).
2. **Debug signing is broken out of the box** — `debugConfig` points at `${rootDir}/debug.keystore`, which is gitignored and absent. README's workaround is literally "remove the `signingConfig` line".
3. **Release signing** depends on `KEYSTORE_PATH` / `STORE_PASSWORD` / `KEY_PASSWORD` env vars; defaults to `${rootDir}/my-upload-key.jks`.
4. **Unused toolchain weight** — Room's KSP compiler and Moshi codegen are enabled, but there is **zero** Room database/entity/DAO code and zero Moshi-annotated model, so KSP currently processes nothing.
5. **Version skew** — Compose BOM 2024.09.00 paired with AGP 9.1.1 / Kotlin 2.2.10 is unusually old for the toolchain and is a likely source of future incompatibility.
6. `google-services` runs with `missingGoogleServicesStrategy = WARN`, so no `google-services.json` is expected/allowed — meaning Firebase cannot actually initialise until one is added.

---

## 3. Application architecture

Everything lives in **one file**: `app/src/main/java/com/example/MainActivity.kt` — **2,740 lines**, ~25 top-level composables. There is no `viewmodel/`, `data/`, `domain/`, `network/`, or `navigation/` package. Only `ui/theme/` is separated (3 small files).

### Navigation
A hand-rolled state machine, not Navigation-Compose:

```kotlin
enum class GuardianScreen { SPLASH, ONBOARDING, LOGIN, PERMISSIONS, HOME, PROFILE, ALARM_OVERLAY }
```

`GuardianApp()` holds `currentScreen` + `previousScreen` and swaps screens inside an `AnimatedContent` with a 400 ms crossfade. All app state (`email`, `password`, `biometricEnabled`, `twoFactorEnabled`, `alertConfigEnabled`) is hoisted into `GuardianApp` as `remember { mutableStateOf(...) }` — i.e. **plain Compose state, no ViewModel, no persistence**; everything resets on process death.

Flow: `SPLASH → ONBOARDING → LOGIN → PERMISSIONS → HOME ⇄ PROFILE`, with `ALARM_OVERLAY` reachable from HOME and PROFILE (returns to `previousScreen`).

### Screens

| Screen | What it does | Reality check |
|---|---|---|
| **SplashScreen** | 2.5 s delay, canvas radar + shield, animated shimmer bars, "v2.4.0" | Pure animation, fine |
| **OnboardingScreen** | Rotating orbital HUD, shield hexagon, title "Defense That Never Sleeps" | Says "01 / 03" and draws 3 dots but only **one page exists**; Skip and Continue both go to LOGIN |
| **LoginScreen** | Email + password `OutlinedTextField`s (password visibility toggle), gradient SIGN IN, "Sign in with Google", forgot/create links | **No authentication at all.** Fields are pre-filled with `operative@guardian.net` / `••••••••`; both buttons just call `onLoginSuccess`; forgot-password and create-account are empty `clickable { }` |
| **PermissionsScreen** | "Step 2 of 6" header, 6-dot progress rail, expandable "why do we need this" card, GRANT ACCESS / SKIP | **Requests no runtime permissions.** Accompanist is commented out, and the manifest declares no permissions. Purely navigational |
| **HomeScreen** | Operator header w/ remote avatar, animated radar "SYSTEM ARMED" button, 3 stat cards (SATS 12 / ALERTS 0 / COVERAGE 98%), "Safe Zone" card with satellite image + canvas crosshairs + pulsing location pin | All metrics **hardcoded**; map, avatar and stats are static; "Update Safe Location" is a no-op |
| **ProfileScreen** | Rotating gradient avatar ring, "ALEX VANCE / OPERATIVE ID: 893-X-11", PRO + VERIFIED badges, Security Protocols card (biometric switch, 2FA, active sessions), System Preferences (alert config, region), Recent Logs, DISCONNECT | Biometric + alert toggles are real switches; **2FA row shows a hardcoded "OFF"** label even though it toggles internal state — a visible bug. Logs are hardcoded literals |
| **AlarmOverlayScreen** | Full-screen lockdown: blinking breach banner, live `HH:mm:ss` clock, heartbeat red glow, shield icon, 4 PIN dots, numeric keypad with letter sub-labels, shake animation on wrong PIN | PIN is **hardcoded `"1234"`**; CANCEL exits; success flashes "GRID ACCESSIBLE" then unlocks |
| **GuardianBottomNavBar** | Custom 3-tab bar (HOME / FEATURES / PROFILE) | Hand-built instead of Material3 `NavigationBar`; **FEATURES tab is a no-op stub** |
| **Simulate breach pill** | A red "SIMULATE BREACH" pill is floated over HOME and PROFILE | This is a **dev/demo affordance left in the production UI** (it overlaps content at `top = 80.dp`) |

**Reusable pieces:** `SettingsSectionHeader`, `SettingsToggleRow`, `SettingsValueRow`, `LogItemRow`, `GuardianBottomNavBar`, `NavBarItem`.

---

## 4. Design system

`ui/theme/Color.kt` defines a genuinely good-looking custom dark palette: Void Black `#050507`, Slate `#131316`, cyber-cyan secondary `#49FCD9`, periwinkle primary `#B6C4FF`, alert red `#FF5167`, plus full surface/outline/error scales.

But in practice:
- `Theme.kt` **forces dark**, ignores its own `darkTheme`/`dynamicColor` parameters, and `LightColorScheme` is declared and never used. `Build`, `isSystemInDarkTheme`, `dynamicDark/LightColorScheme`, `lightColorScheme`, `LocalContext` are **unused imports**.
- `MainActivity.kt` hardcodes **246 `Color(0xFF…)` literals** instead of reading `MaterialTheme.colorScheme`. The theme is therefore almost decorative, and the same hex values are duplicated across the file.
- `Type.kt` only overrides `bodyLarge`; everything else is Material default. No custom font despite the "tactical" aesthetic.
- All icons are Material icons; Coil loads three remote `lh3.googleusercontent.com` images.

**Manifest issue:** there is **no `<uses-permission android:name="android.permission.INTERNET" />`**. Coil's remote images will fail to load on device, and any future Retrofit/Gemini call would fail too. Also, the app uses `android:Theme.DeviceDefault.NoActionBar` and no network-security config.

---

## 5. Tests — currently all broken

| Test | State |
|---|---|
| `ExampleUnitTest` | Trivial `2 + 2`; passes |
| `ExampleRobolectricTest` | Asserts `app_name == "My Application"`, but `strings.xml` says **"Guardian"** → **fails** (stale AI Studio scaffold) |
| `GreetingScreenshotTest` | Calls `Greeting("Robolectric")` — **that composable does not exist anywhere in the codebase** → **won't compile** |
| `ExampleInstrumentedTest` | Asserts `packageName == "com.example"`, but applicationId is `com.aistudio.guardian.tactical` → **fails on device** |
| `src/test/screenshots/greeting.png` | A stale 2.8 KB artifact from the template |

So the Roborazzi screenshot infrastructure is installed and working conceptually, but there is **no test covering any real Guardian screen**.

---

## 6. Code-quality observations (prioritised)

**High**
1. 2,740-line single file mixing 7 screens + design tokens + navigation — no module boundaries, very hard to review/diff. Natural split: `ui/screens/*`, `ui/components/*`, `ui/theme/*`, `navigation/*`.
2. No ViewModel / state layer: auth + settings state dies with the process, and nothing is persisted (DataStore and Room are both unused).
3. `INTERNET` permission missing while remote images/deps need it.
4. Tests don't compile/pass, which blocks any CI gate.
5. No Gradle wrapper → no CLI/reproducible builds.

**Medium**
6. 246 hardcoded colors duplicating the theme palette; theme ignores its parameters.
7. Fake auth flow that "succeeds" on any tap — dangerous if this is ever treated as real security. Same for the permissions screen that grants nothing.
8. `SIMULATE BREACH` demo pill is shipped in the normal UI.
9. `GEMINI_API_KEY` / Firebase AI / Retrofit / Moshi / Room declared but completely unused — dead dependency surface and unreachable Firebase init without `google-services.json`.
10. Onboarding claims 3 pages, ships 1; permissions claims 6 steps, shows 1; nav has a dead FEATURES tab — several half-built features.

**Low**
11. Unused imports in `Theme.kt`; unused `signingConfigs.release` aliasing; `libs.versions.toml` carries ~40 versions of which many are commented out.
12. Accessibility: many `Icon`s have descriptive `contentDescription`s (good), but custom `clickable` Boxes/Columns have no `role`/`onClickLabel`, and several touch targets are < 48 dp (PIN keypad uses `aspectRatio(1f)` in a 280 dp row → ~76 dp, OK; but top-bar icon buttons at 40 dp are borderline).
13. No localization: strings are inline literals, `strings.xml` has only `app_name`.

---

## 7. What's genuinely good

- The **visual design is strong and consistent** — coherent neumorphic/HUD language, animated radar canvases, staged glow/`infiniteTransition` work, gradient CTAs, `scale`/`rotate`/`shadow` polish.
- **Clean composable decomposition** at the screen level, with callbacks instead of direct state mutation, and hoisted state from day one — so refactoring into a ViewModel is straightforward.
- The lockdown keypad (PIN dots, shake spring, live clock) is the most "complete" feature and shows the intended feel.
- Version catalog + convention-style build files, `enableEdgeToEdge` + `safeDrawingPadding` used properly, RTL support declared.

---

## 8. Constraints of this environment

- There is **no JDK or Gradle in the sandbox** and the project has **no wrapper**, so I cannot compile, run Roborazzi, or produce a build artifact here. Any change I make has to be verified by inspection (and I'll keep edits conservative), unless you want me to install a toolchain — which still won't give an APK without the Android SDK.
- Because this is an Android app, the "live preview" server tooling doesn't apply; nothing runs in the browser.

---

## 9. Suggested next steps (pick one and I'll start)

1. **Refactor the monolith** into `ui/screens/`, `ui/components/`, `navigation/` without changing behaviour — biggest quality win, low risk.
2. **Fix the foundation:** add `INTERNET` permission, repair/rewrite the three broken tests (and add real Roborazzi screenshots for Splash/Home/Profile/Lockdown), add the Gradle wrapper, fix the 2FA label bug.
3. **Design system pass:** replace the 246 literals with `MaterialTheme.colorScheme` + `GuardianTheme` tokens, wire up typography, finish `Theme.kt` (remove dead params/imports).
4. **Make it real:** real email/Google auth + DataStore session, actual runtime permission requests, and a Gemini "threat advisor" screen using the existing `firebase-ai` + `GEMINI_API_KEY` setup.
5. **Build out the half-finished flows:** 3-page onboarding pager, 6-step permission wizard, FEATURES tab, and replace the hardcoded PIN with a user-set one stored securely.

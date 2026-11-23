# Screen-by-Screen Rebuild Specification

Format per screen: **Purpose → Kotlin reference → Flutter tree → States → Measurements → Acceptance**.

Global assumptions: dark-only theme; `SafeArea` top/bottom; horizontal padding 24 dp (auth/onboarding) or 20 dp (home/profile); all type tokens from `05-DESIGN-SYSTEM.md`.

---

## 1. Splash — `features/splash/splash_screen.dart`

**Purpose:** brand moment + boot gate.
**Kotlin:** L205–353. `delay(2500)` → onboarding. 300 dp radial glow, 160 dp CircleCanvas (outer ring α.2 r=0.4w·1.15, inner filled α.15), 70 dp `Shield` icon, "GUARDIAN" 32 sp/800/letterSpacing 8, "ANTI-THEFT INTELLIGENCE" 10 sp/700/ls 4, two shimmer bars (120 dp wide, 2 dp tall), "v2.4.0" 12 sp bottom 32 dp.

**Flutter tree**
```
Scaffold(backgroundColor: voidBlack)
└ Stack(alignment: center)
  ├ _GlowDisc(300, radial cyan α.08)            // DecoratedBox, RadialGradient
  ├ Column(center)
  │  ├ SizedBox(160) → CustomPaint(_SplashRadarPainter)
  │  │                 + Icon(Icons.shield, 70, cyan)
  │  ├ Text('GUARDIAN', …)  // displaySmall
  │  ├ Text('ANTI-THEFT INTELLIGENCE', …) // labelMicro
  │  └ SizedBox(height: 48) + _ShimmerBars(progress)
  └ Align(bottomCenter, padding 32) → Text(version)
```
**Boot gate** (replaces the fake 2500 ms):
```dart
final results = await Future.wait([
  initFirebase(), readSecureStore(), Future.delayed(900.ms),
]);
if (!mounted) return; context.go(seenOnboarding ? '/login' : '/onboarding');
```
**States:** booting → (ready | bootError→retry CTA).
**Acceptance:** shimmer loops seamlessly; version reads from `PackageInfo`; no 2.5 s dead wait.

---

## 2. Onboarding — `features/onboarding/`

**Purpose:** 3-beat value pitch (Kotlin faked this with 1 page).
**Kotlin:** L355–611. Rotating 220 dp orbit (12 s), hexagon 140 dp + `AdminPanelSettings` 48, three 28 dp chips at TopCenter/BottomStart/BottomEnd, "01 / 03" cyan 12/700/ls2, title 26/800, body 14/500 max 280 dp, 3 dots (active 24×5), ghost "Continue", "Skip" pill top-right.

**Flutter tree**
```
Scaffold
└ SafeArea
  └ Column
    ├ Row(end) → _SkipPill                       // hidden on page 3
    ├ Expanded → PageView(controller, onPageChanged)
    │   └ _OnboardingPage ×3
    │      ├ Expanded → CustomPaint(OrbitalPainter)  // 240 dp box
    │      ├ Text('0${i+1} / 03')
    │      ├ Text(title)                        // headlineSmall
    │      └ Text(body, maxWidth 280)
    └ Row(spaceBetween)
       ├ _PageDots(activeIndex)                 // AnimatedContainer 5↔24
       └ PrimaryGhostButton('Continue', onTap)
```
**Copy (3 pages)** — see `00-MASTER-PLAN.md §5.1`.
**States:** first-launch only; `seenOnboarding` persisted in secure store.
**Acceptance:** swipe and button both work; dots animate; Skip hidden on last page; back gesture returns to previous page before exiting.

---

## 3. Login — `features/auth/login_screen.dart`

**Purpose:** authenticate. Kotlin's version authenticates nobody.
**Kotlin:** L613–909. Top radial glow (450 dp, α.06, offset −150), logo row, "Welcome Back." 32/800, subtitle 14, two `OutlinedTextField` (r14, fill `#0C0E1A` α.4, focus border `#668AFF`), "Forgot password?" 12, gradient CTA 52 dp (`#3D6FFF`→`#4DFFDB`), OR divider, Google button 48 dp, "New here? Create account".

**Flutter tree**
```
Scaffold → SingleChildScrollView → Column(center)
├ _TopGlow()
├ _BrandRow(Icon shield 24 + 'GUARDIAN')
├ Text('Welcome Back.') / Text('Authenticate to access the tactical grid.')
├ Form(key) → Column(gap16)
│   ├ _GuardianTextField(label:'Email Address', prefix: mail, validator)
│   └ _GuardianTextField(label:'Password', obscure, suffix: visibility toggle)
├ Row(end) → TextButton('Forgot password?')
├ PrimaryGradientButton('SIGN IN', loading, onPressed: _submit)
├ _OrDivider()
├ GoogleSignInButton()                        // official branding, not Icons.android
└ Row('New here? ' + 'Create account' link)
```
**States:** idle · submitting(spinner in CTA, fields disabled) · fieldErrors · authError(banner + `Semantics(liveRegion)`) · lockedOut(countdown).
**Validation:** email regex + non-empty; password ≥8. Rate limit 5 tries → 30 s.
**Acceptance:** wrong password shows a real error from the repo; no credentials pre-filled; keyboard avoids CTA (`scrollPadding`); autofill hints set.

---

## 4. Permissions — `features/permissions/`

**Purpose:** obtain the 6 capabilities, honestly.
**Kotlin:** L911–1276 — a beautiful 6-step rail with no requests behind it.
**Flutter tree**
```
Scaffold → Column
├ _WizardHeader(step, total, onBack)            // 'Step n of 6'
├ _StepRail(current: step, total: 6)            // 6 dots + connectors
├ Expanded → PageView(physics: NeverScrollable)
│   └ _PermissionStep ×6 (icon, title, why, systemState chip)
├ _SystemStateCard(status)                      // GRANTED / PENDING / BLOCKED
├ PrimaryGradientButton('GRANT ACCESS' | 'OPEN SETTINGS')
└ TextButton('SKIP FOR NOW')
```
**Controller:** `permission_handler` → returns `granted|denied|permanentlyDenied|restricted`.
**Step list & rationale:** see `01-FEATURE-RESEARCH.md §F4` table. **Rewrite every rationale in plain language** — Google rejects vague justifications.
**States:** notAsked → requesting → granted → denied(soft) → permanentlyDenied(hard, CTA → `openAppSettings()`) → restricted(iOS parental).
**Acceptance:** every button changes real OS state; resumable; blocked steps can't be faked as done; "Skip" never marks granted.

---

## 5. Home — `features/home/`

**Purpose:** the armed-state dashboard. Highest-value screen.
**Kotlin:** L1278–1791 (514 LOC). Header (44 dp avatar w/ 2 dp cyan ring, "Good evening, John", "OPERATOR ACTIVE"), bell 40 dp with red dot, 240 dp radar, 3 stat cards, Safe Zone card (160 dp map + crosshairs + dashed r45 circle + center dot + expanding pulse 0→60 dp + pin, then 48 dp outline CTA).

**Flutter tree**
```
Scaffold(bottomNavigationBar: GuardianBottomNav(0))
└ CustomScrollView
  ├ SliverToBoxAdapter → _OperatorHeader(avatar, greeting, bell(badge))
  ├ SliverToBoxAdapter → _SystemCore(size:240)     // GestureDetector → /lockdown
  ├ SliverToBoxAdapter → Row(_StatCard×3, gap10)    // SATS / ALERTS / COVERAGE
  └ SliverToBoxAdapter → _SafeZoneCard()
```
**_SystemCore internals**
```
Stack(center)
├ CustomPaint(RadarPainter(outerAngle, innerAngle, glowScale))
│    // ring1 α.12 stroke1; ring2 α.25 stroke1.5 dash(15,25) rotate24s
│    // ring3 α.15 stroke1 dash(6,12) rotate16s reverse
├ Transform.scale(glowScale) → _ArmedButton(130)   // BoxShadow cyan blur20
│    └ Column(Icon shield 36, 'SYSTEM ARMED' 11/700/ls1)
└ (optional) _StatusRing(state)                    // armed/offline/lockdown colour
```
**Real state machine** (fixes the "always ARMED" lie):
```dart
enum ArmedState { armed, disarmed, lockdown, offline, degraded }
```
Colour map: armed=cyan, disarmed=outline, lockdown=alert, offline=warning amber, degraded=periwinkle.
**Stats** from `homeStatsProvider`: real GNSS count, unresolved alert count, geofence coverage %— each with an "unavailable" render (`'—'`) rather than a fake number.
**Safe Zone:** bundle `assets/illustrations/safe_zone_map.png`; draw via `SafeZoneMapPainter` (2 hairlines α.15, dashed circle r45 α.3 dash(10,10), 4 dp centre dot, pulse ring 0→60 dp α .35·(1−t/60)); pin 24 dp offset −6; CTA → `/safe-zone` editor.
**Acceptance:** 60 fps with all three animations live; tap core → lockdown; stats never fabricate; works at 320 dp width without overflow.

---

## 6. Profile — `features/profile/`

**Purpose:** identity + security configuration + audit trail.
**Kotlin:** L1793–2129. Rotating 86 dp sweep-gradient ring (4 s), 80 dp avatar, "ALEX VANCE" 28/800, "OPERATIVE ID: 893-X-11" 11/700/ls1.5, PRO + VERIFIED badges, Security Protocols (biometric switch / 2FA / sessions), System Preferences (alert config / region), Recent Logs ×2, DISCONNECT (red outline 52 dp).

**Flutter tree**
```
Scaffold(bottomNavigationBar: GuardianBottomNav(2))
└ CustomScrollView
  ├ _BrandBar(shadow icon + 'GUARDIAN' + bell)
  ├ _AvatarRing(90)                    // SweepGradient cyan→blue→cyan, 4 s
  ├ Text(operator.name) / Text('OPERATIVE ID: ${operator.id}')
  ├ Row(_Badge.pro, _Badge.verified)
  ├ SectionHeader('SECURITY PROTOCOLS')
  │  └ GlassCard → SettingsRow.toggle(Biometric) / .value(Two-Factor, derived) / .value(Sessions, '3 DEVICES', → /sessions)
  ├ SectionHeader('SYSTEM PREFERENCES')
  │  └ GlassCard → SettingsRow.toggle(Alerts) / .value(Region, 'EN-US', → picker)
  ├ SectionHeader('RECENT LOGS')
  │  └ ListView(LogTile×n from auditLogProvider)   // empty state illustration
  └ _DangerButton('DISCONNECT', Icons.logout, onTap: confirm dialog)
```
**Fixes carried in:** 2FA label derived from state; single source for operator name; logs from drift, not literals; sessions open a real revoke list.
**Acceptance:** toggles persist across restart; badge/tier from entitlements; disconnect confirms and clears secure store; long log lists scroll inside the page without nested-scroll jank.

---

## 7. Lockdown — `features/lockdown/lockdown_screen.dart`

**Purpose:** the "custom lockdown protocol" — the app's signature moment.
**Kotlin:** L2329–2629. Breach banner 32 dp (marquee + live `HH:mm:ss`), heartbeat radial α.05↔.20 (1500 ms), 160 dp orbit + 80 dp shield w/ coloured shadow, "DEVICE LOCKED" 32/800, 4 dots 14 dp gap20, 280 dp keypad 4×3 (aspect 1, CANCEL/DEL), spring shake, PIN `"1234"`.

**Flutter tree**
```
Scaffold
└ Stack(fill)
  ├ _HeartbeatGlow()                  // RadialGradient α animate .05↔.20
  ├ Column
  │  ├ _BreachBanner(height32)        // text + clock from clockProvider
  │  ├ Expanded → Transform.translate(shakeDx)
  │  │   └ Column(center)
  │  │      ├ CustomPaint(160, _LockOrbitPainter)
  │  │      │   + Icon(shield 80, colour by state)
  │  │      ├ Text('DEVICE LOCKED' | 'GRID ACCESSIBLE')
  │  │      ├ _PinDots(6, filled, error, success)
  │  │      └ _Keypad(288)            // 4×3, letter sub-labels, haptics
  │  └ Spacer(40)
  └ (bottom) TextButton('Use biometrics')   // new: fallback
```
**PIN logic (replaces `== "1234"`)**
```dart
Future<PinResult> verify(String pin) async {
  final rec = await store.readPinRecord();       // salt + hash + attempts
  if (rec.lockedUntil.isAfter(now)) return PinResult.lockedOut;
  if (hash(pin, rec.salt) == rec.hash) { await store.resetAttempts(); return PinResult.ok; }
  final n = rec.attempts + 1;
  await store.bumpAttempts(n);                    // 5 → 30 s, doubling, cap 1 h
  if (n == 3) await cameraService.captureIntruderSnapshot();  // optional
  return n >= 5 ? PinResult.lockout : PinResult.wrong;
}
```
**Shake:** `TweenSequence` [+20 → −20 → 0] × `Curves.elasticOut`, ~600 ms total (Kotlin's 3 sequential springs ≈ this).
**Reachability:** route `/lockdown?reason=…`; can be raised by notification tap, `AppLifecycleState.paused` watchdog, or the Home core button.
**Accessibility:** each key `Semantics(label:'Digit n')`; live region announces digit count; respect `disableAnimations`.
**Acceptance:** wrong PIN shakes once and clears dots; error colour visible ≥1.2 s; lockout shows a real countdown; biometric fallback works; back button does **not** dismiss; `WillPopScope`/`PopScope(canPop:false)`.

---

## 8. Shell / nav — `features/shell/guardian_bottom_nav.dart`

80 dp bar, `surfaceContainer` α.6, 0.5 dp white α.1 top border, 3 items spaced around. Active item = cyan pill (r12, α.15 fill, α.3 border) with icon+label stacked; inactive = icon+label in `onSurfaceVariant` α.6.
Flutter: hand-built (as in Kotlin) rather than `NavigationBar`, to preserve the pill look; wrap in `SafeArea(top:false)`; each item `Semantics(selected:)`.
**Tabs:** HOME → `/home`, FEATURES → `/alerts`, PROFILE → `/profile`.

---

## 9. New screens (not in Kotlin)

| Screen | Why | Minimum spec |
|---|---|---|
| `/alerts` | Replaces dead tab; the product's missing centre | Chronological merged feed, severity filter chips, day separators, empty state |
| `/safe-zone` | "UPDATE SAFE LOCATION" was a no-op | Map + draggable centre + radius slider 100–2000 m + save → geofence service |
| `/sessions` | Promised by "3 DEVICES" row | Device, last-seen, current badge, revoke button |
| `/advisor` | Justifies the `firebase-ai` dependency | Streaming chat, suggested prompts, scope banner ("advises, never acts") |
| `/settings/pin` | Needed to replace hardcoded PIN | Set/change 6-digit PIN, confirm, recover via biometrics+password |

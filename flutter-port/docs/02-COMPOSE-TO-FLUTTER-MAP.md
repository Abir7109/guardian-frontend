# Compose → Flutter Translation Guide

Every mapping below is derived from **actual code in this repo**, not generic advice. Line references are to `app/src/main/java/com/example/MainActivity.kt` at `fd0e6d6`.

---

## 1. Mental model shift

| Compose | Flutter | Consequence for the port |
|---|---|---|
| `@Composable fun Foo()` | `class Foo extends StatelessWidget` or `StatefulWidget` | No inline "scope" DSL. Layout = nested constructors, so deep trees get noisy → extract early |
| `Modifier` chain | Wrapper widgets / `DecoratedBox` / `Container` | `Modifier.padding().background().border().clip()` becomes an *ordering-sensitive* nesting of `Padding`→`DecoratedBox`→`ClipRRect`. **Widget order changes render order** — get it wrong and borders clip |
| `remember { mutableStateOf(x) }` | `StatefulWidget` + `setState`, or **Riverpod provider** | Use Riverpod for anything crossing a screen boundary (mimics the hoisted state in `GuardianApp()`) |
| `LaunchedEffect(Unit) {}` | `initState` + `WidgetsBinding.instance.addPostFrameCallback`, or `ref.listen` | Timers must be cancelled in `dispose` — Compose does this automatically, Flutter does not |
| `AnimatedContent` | `AnimatedSwitcher` or go_router `CustomTransitionPage` | Prefer router-level transitions so back-gesture works |
| `rememberInfiniteTransition().animateFloat` | `AnimationController.repeat()` | One controller can drive many values via `Tween` + `CurvedAnimation` — much cheaper |
| `animate*AsState` | `TweenAnimationBuilder` / `AnimatedContainer` | — |
| `spring(dampingRatio, stiffness)` | `SpringSimulation` or `Curves.elasticOut` | Not 1:1; see §6 for the shake |
| `Canvas { drawCircle(...) }` | `CustomPaint(painter: ...)` | `DrawScope` ≈ `Canvas` API, but Compose helpers differ (see §4) |
| `PathEffect.dashPathEffect` | **no direct equivalent** | Must hand-roll with `PathMetric.extractPath` (see §5) |
| `Icons.Default.Shield` | `Icons.shield` (`material_icons` / `material_symbols_icons`) | Mostly 1:1, snake_case. Some have no Material equivalent → use `flutter_svg` or a bundled SVG |
| `AsyncImage(model = ImageRequest…)` | `CachedNetworkImage` / `Image.asset` | Replace the 3 unstable `lh3.googleusercontent.com` URLs with bundled assets |
| `enableEdgeToEdge()` + `safeDrawingPadding()` | `SystemChrome.setEnabledSystemUIMode(edgeToEdge)` + `SafeArea` | Do it once in `AppBar`-less scaffolds |
| `MaterialTheme.colorScheme` | `Theme.of(context).colorScheme` | The Kotlin app bypasses it with 246 literals — **don't** repeat that |

---

## 2. Screen-level layout mappings

| Kotlin (line) | Compose | Flutter |
|---|---|---|
| `GuardianApp` L76 | `Box` + `AnimatedContent(targetState)` | `MaterialApp.router` + `CustomTransitionPage` with `FadeTransition` 400 ms |
| `GuardianScreen` enum L65 | `enum class` + `when` | `enum AppRoute` + GoRouter paths (`/`, `/onboarding`, `/login`, `/permissions`, `/home`, `/profile`, `/lockdown`) |
| `previousScreen` L77 | manual back-stack var | `context.pop()` / `GoRouter.pop()` — delete the manual var |
| `SplashScreen` L205 | `Column` + `Arrangement.Center` + `Spacer` | `Column(mainAxisAlignment: MainAxisAlignment.center, children:[...Spacer()])` |
| `Box(contentAlignment = Alignment.Center)` | `Box` overlay with alignment | `Stack(alignment: Alignment.center, children: [...])` |
| `Row(horizontalArrangement = SpaceBetween)` | `Row` | `Row(mainAxisAlignment: MainAxisAlignment.spaceBetween)` |
| `Arrangement.spacedBy(16.dp)` | `Row`/`Column` gap | `Wrap`/`Column` **has no gap** → use `SizedBox` or `separatorBuilder` in `ListView` |
| `Column(verticalScroll(rememberScrollState()))` | scrollable column | `SingleChildScrollView(child: Column(...))` or `CustomScrollView` |
| `Column(Modifier.weight(1f))` | `weight` | `Expanded(flex: 1)` / `Flexible` |
| `LazyColumn` + `items` | Compose list | `ListView.builder(itemBuilder:)` |
| `Modifier.aspectRatio(1f)` | — | `AspectRatio(aspectRatio: 1)` |
| `Modifier.offset(x/y)` | — | `Transform.translate(offset: Offset(x,y))` (paint-only, doesn't affect layout — **same semantics as Compose**) |
| `Modifier.rotate(angle)` | — | `Transform.rotate(angle: radians)` — Compose takes **degrees**, Flutter takes **radians** |
| `Modifier.scale(1.1f)` | — | `Transform.scale(scale: 1.1)` |
| `Modifier.shadow(elevation, ambientColor, spotColor)` | coloured glow | `BoxShadow(color: c.withOpacity(.5), blurRadius: 20, spreadRadius: 0)` in a `DecoratedBox` — Flutter's shadows have one colour, so pick the glow colour |
| `Modifier.clip(CircleShape)` | — | `ClipOval` |
| `Modifier.clip(RoundedCornerShape(14.dp))` | — | `ClipRRect(borderRadius: BorderRadius.circular(14))` |
| `Modifier.border(0.5.dp, color, shape)` | hairline border | `Container(decoration: BoxDecoration(border: Border.all(color: c, width: 0.5), borderRadius: ...))` |
| `Brush.radialGradient(colors, radius)` | - | `RadialGradient(colors: [...], radius: 0.5)` inside `BoxDecoration` (radius is **fractional**, not px) |
| `Brush.linearGradient(colors)` | - | `LinearGradient(colors: [...])` |
| `Brush.sweepGradient(colors)` | avatar ring | `SweepGradient(colors: [...])` |
| `Card(border = BorderStroke(...))` | M3 card | `Card(shape: RoundedRectangleBorder(side: BorderSide(...)))` |
| `OutlinedTextField` | M3 text field | `TextFormField(decoration: InputDecoration(border: OutlineInputBorder(...)))` |
| `Switch(colors = SwitchDefaults.colors(...))` | M3 switch | `Switch(activeColor:…, activeTrackColor:…)` or a custom `CupertinoSwitch` for tighter control |
| `Divider(thickness = 0.5.dp)` | - | `Divider(height: 1, thickness: 0.5, color: ...)` |
| `AnimatedVisibility(expandVertically())` | expand/collapse | `AnimatedSize` + `AnimatedOpacity`, or `SizeTransition` |
| `LazyColumn` logs | list | `ListView.separated` |
| `TextButton` / `OutlinedButton` / `Button` | M3 buttons | `TextButton` / `OutlinedButton` / `ElevatedButton`, or custom `PrimaryGradientButton` (needed — the Kotlin app uses a gradient *inside* a transparent button, which Flutter can't do with ButtonStyle alone) |

---

## 3. Colour & opacity

Kotlin: `Color(0xFF49FCD9).copy(alpha = 0.12f)`
Dart: `GuardianColors.cyan.withValues(alpha: 0.12)`

> ⚠️ `Color.withOpacity()` is **deprecated** in current Flutter in favour of `withValues(alpha:)`, which is colour-space aware. Use `withValues` throughout so you don't inherit a wall of deprecation warnings.

Also: `Color(0xFFRRGGBB)` → `Color(0xFFRRGGBB)` is **identical** in Dart. The hex values port literally — this is the one thing that is byte-for-byte.

---

## 4. Canvas / drawing API deltas

| Compose `DrawScope` | Flutter `Canvas` | Gotcha |
|---|---|---|
| `drawCircle(color, radius, center, style=Stroke(w))` | `canvas.drawCircle(center, radius, Paint()..style=stroke..strokeWidth=w..color=c)` | Compose `radius` is a `Float`; Flutter needs `Offset` center |
| `drawLine(color, start, end, strokeWidth)` | `canvas.drawLine(start, end, paint)` | — |
| `drawPath(path, color, style)` | `canvas.drawPath(path, paint)` | Compose `Path()` and `dart:ui` `Path()` have near-identical APIs (`moveTo`, `lineTo`, `close`) |
| `drawRect` / `drawRoundRect` | `canvas.drawRect` / `drawRRect(RRect.fromRectAndRadius(...))` | — |
| `Stroke(cap = StrokeCap.Round)` | `paint.strokeCap = StrokeCap.round` | — |
| `size.width` inside `DrawScope` | `size.width` param of `paint(Canvas, Size)` | Same name, different source |
| `1.dp.toPx()` | `1 * MediaQuery.devicePixelRatioOf(context)` **or** pass density into the painter | **Biggest trap.** Painters have no `BuildContext`; read `MediaQuery.devicePixelRatio` in the widget and pass `scale` into the painter |

**Golden rule for painters:** make every dimension a *fraction of `size`* where possible (the Kotlin code already half-does this: `radius = width * 0.4f`), and take a `pixelRatio` parameter for the hairline strokes.

---

## 5. The one thing Flutter genuinely lacks: dashed paths

Kotlin uses `PathEffect.dashPathEffect(floatArrayOf(15f, 25f), 0f)` in three places:
- Home radar outer ring: `(15, 25)`
- Home inner ring: `(6, 12)`
- Safe-zone circle: `(10, 10)`

Flutter's `Paint` has **no** dash support. You must extract dash segments yourself:

```dart
Path dashPath(Path source, {required double dash, required double gap}) {
  final dest = Path();
  for (final metric in source.computeMetrics()) {
    double distance = 0;
    while (distance < metric.length) {
      final next = distance + dash;
      dest.addPath(metric.extractPath(distance, next.clamp(0, metric.length)), Offset.zero);
      distance = next + gap;
    }
  }
  return dest;
}
```

Ship this once in `core/utils/dash.dart` and reuse. **Intervals must be converted from dp to px** (`15.dp` → `15 * pixelRatio`), otherwise dashes look half-size on high-density screens — a bug the Compose version doesn't have because `toPx()` handles it.

---

## 6. Animation timings (copy these values exactly)

These are lifted from the Kotlin source — the ports should be visually indistinguishable:

| Element | Kotlin spec | Flutter implementation |
|---|---|---|
| Screen transition | `tween(400)` fade in/out | `FadeTransition` 400 ms, `Curves.easeInOut` |
| Splash shimmer | 1500 ms, `LinearEasing`, restart | `AnimationController(duration: 1500ms)..repeat()` |
| Splash delay | `delay(2500)` | gate: `Future.wait([init, delay(900)])` → trim to taste |
| Onboarding orbit | 12000 ms, linear, 0→360° | `..repeat()` + `Transform.rotate` |
| Home outer ring | 24000 ms 0→360 | controller A, `repeat()` |
| Home inner ring | 16000 ms 360→0 | controller B, `reverse: true` via `Tween(360,0)` |
| Home glow pulse | 2000 ms, `EaseInOutSine`, reverse | `Curves.easeInOutSine` + `..repeat(reverse: true)`, scale 0.95→1.05 |
| Safe-zone pulse | 2000 ms, `EaseOutQuad`, restart, 0→60 dp | `Curves.easeOutQuad` + `..repeat()` |
| Profile avatar ring | 4000 ms linear 0→360 | `..repeat()` |
| Lockdown heartbeat | 1500 ms `EaseInOutSine` reverse, alpha .05→.20 | `..repeat(reverse: true)` |
| Lockdown clock | `SimpleDateFormat("HH:mm:ss")` each 1000 ms | `Timer.periodic(1s)` in a provider |
| **Shake on wrong PIN** | `spring(DampingRatioHighBouncy, StiffnessMedium)` → +20, then −20, then 0 (3 sequential `animateTo`) | `AnimationController` + a **piecewise curve**: `TweenSequence([+20, −20, 0])` with `Curves.elasticOut` per leg ≈ visually equivalent |

`EaseInOutSine` in Compose ≈ `Curves.easeInOutSine` in Flutter. `EaseOutQuad` ≈ `Curves.easeOutQuad`. `LinearEasing` = `Curves.linear`. These are all 1:1.

---

## 7. State: `remember` → Riverpod

Kotlin's `GuardianApp()` hoists six values (L80–L86) and passes them down as params. Flutter equivalent:

```dart
// BEFORE (Kotlin)
var biometricEnabled by remember { mutableStateOf(true) }
// ...
ProfileScreen(biometricEnabled = biometricEnabled, onBiometricToggle = { biometricEnabled = it })

// AFTER (Flutter + Riverpod)
@riverpod
class SettingsController extends _$SettingsController {
  @override
  SettingsState build() => ref.watch(secureStoreProvider).readSettings();
  Future<void> setBiometric(bool v) async { ... state = state.copyWith(biometric: v); }
}
// in the widget
final s = ref.watch(settingsControllerProvider);
SettingsRow.toggle(value: s.biometric, onChanged: (v) => ref.read(settingsControllerProvider.notifier).setBiometric(v));
```

Benefits you gain over the Kotlin version, all of which fix real defects there:
- state **survives process death** (the Kotlin app resets everything)
- toggles **persist** (Kotlin's are lost on recreate)
- a widget can't accidentally desync a label from its state (the `"OFF"` 2FA bug)

---

## 8. Accessibility parity checklist

Kotlin gets some of this right (most `Icon`s have `contentDescription`) but misses the rest. In Flutter:

| Kotlin | Flutter |
|---|---|
| `contentDescription = "…"` | `Semantics(label: '…')` or `Icon(semanticLabel: '…')` |
| — | `Semantics(button: true, enabled: true)` on custom tappables |
| — | `Tooltip(message: …)` for icon-only buttons |
| — | Respect `MediaQuery.textScaler` — never hard-code `Text` heights |
| — | `MediaQuery.disableAnimations` → freeze infinite transitions (Android "remove animations") |

The PIN keypad is the highest-risk surface: it needs `Semantics(label: 'Digit 1')`, a `FocusTraversalGroup`, and a "PIN entry" live region announcing "2 of 6 digits entered". Compose had none of this.

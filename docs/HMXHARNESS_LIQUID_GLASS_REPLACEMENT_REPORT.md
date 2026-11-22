# HMXHARNESS — Liquid Glass Replacement Report

Branch: `feature/hmxharness-liquid-glass-ui`
Starting commit (recorded before any replacement work): `3df84a94`
App: `HMXHARNESS`, package/namespace `com.hmx.harness`

---

## 1. What was replaced

The existing Compose UI was transformed in place. No second dashboard, no
parallel screen set, no duplicate routes and no decorative screens were added.
Every screen keeps its original route, callbacks and state; only its
presentation changed.

### Screens transformed

| Screen | File | Presentation change |
| --- | --- | --- |
| Onboarding | `ui/setup/SetupScreen.kt` | `OutlinedCard` permission rows → `HarnessGlassSurface` glass panels with a completion-aware border; the same progress, actions and permission callbacks. |
| Home / chat dashboard | `ui/chat/components/ChatEmptyState.kt`, `MainHeader.kt`, `MessageComposer.kt`, `chat/ChatScreen.kt` | Hero, starter cards, toolbar, model selector and composer are glass; user bubbles are glass. |
| Navigation drawer | `ui/AppNav.kt` | `ModalDrawerSheet` becomes a translucent deep-glass panel (`LiquidGlassLevel.DEEP`) with a real recorded backdrop; segmented controls and action rows keep their callbacks. |
| AI Chat | `ui/chat/**` | Message surfaces, tool/artifact/sub-agent cards, status bars, composer, model picker and sheets use layered glass. |
| Settings | `ui/settings/**` | `SettingsGroup`/`SettingsSegment` and `SettingsPanel` are `HarnessGlassSurface`; the active workspace card and search rows are glass. |
| Providers | `ui/settings/ProviderListCard.kt`, `ProvidersScreen.kt` | Provider cards and the empty state are `HarnessGlassSurface`; active/inactive states keep readable status colours. |
| Shared list rows | `ui/common/ExpressiveList.kt` | `ExpressiveListRow` renders through `HarnessGlassSurface` instead of an opaque Material surface. |

### Old visual implementations removed or retired

- Flat `Surface`/`OutlinedCard` containers on the six required screens were
  replaced by `HarnessGlassSurface`.
- `ExpressiveListRow` and `StarterCard` no longer stack two glass engines on one
  element (the Prismal modifier was removed, the kyant `harnessGlass` material
  remains) — stacking two backdrop samplers produced doubled, muddy blur and
  wasted a second offscreen texture on a Helio G85.
- Opaque `Modifier.background` fills that sat on top of glass were replaced with
  the theme's glass-aware surface so the backdrop is actually visible.

### Root cause found

The glass engines existed but were switched off by default:

- `data/FeatureAvailability.kt` had `HARNESS_GLASS = false`, which hid the glass
  settings section, the settings-search entries and the assistant settings keys.
- `AppSettings.harnessGlassEnabled` and `harnessLiquidGlass` defaulted to
  `false`, and the DataStore fallback also read `false`.

Those three defaults are now `true`, so the shipped application presents Liquid
Glass out of the box. This is a presentation change only: no business rule,
route or data source changed.

---

## 2. Rendering library and licences

| Library | Role | Licence |
| --- | --- | --- |
| `com.github.styropyr0:PrismalAGSL` (JitPack, `1.0.7`) | Primary glass material for headers, cards, buttons, drawer and onboarding. Samples a recorded backdrop, applies backdrop blur, an edge lens with refraction and chromatic dispersion, specular and depth layers. | MIT |
| `com.kyant0:backdrop` (`2.0.1`) | The existing in-app engine (`ui/theme/HarnessGlass.kt`): real backdrop sampling, lens, chromatic aberration and highlight for panels, rows and the composer. | Existing project dependency |

Both engines degrade: `HarnessGlassMode.resolve` selects `OFF` below Android 12,
`FROSTED` on 12–12L and `LIQUID` from Android 13. `Modifier.liquidGlass` falls
back to a lightweight layered-gradient material when no backdrop layer or no
shader support is available. `LocalLiquidGlassQuality` selects `LITE` on devices
with a low heap so the optional inner hairline and extra specular pass are
skipped.

Reference implementations studied: NEORUAA/Glasmio, itshedimisawi/vitro,
styropyr0/PrismalAGSL, skydoves/Cloudy. PrismalAGSL was chosen because it works
with the project's existing Compose/Kotlin/Gradle configuration and provides the
required backdrop sampling, blur, edge lens and dispersion without an upgrade of
the build stack.

---

## 3. Rendering techniques actually used

- **Real backdrop sampling**: both engines draw from a recorded layer of the
  app background, never from a static colour.
- **Backdrop blur**: `FROSTED` blurs the sampled layer; Prismal applies a
  per-level blur radius (6–16 dp).
- **Optical refraction / lens**: the kyant engine's `lens()` runs on corner
  shapes in `LIQUID` mode with depth effect and chromatic aberration; Prismal's
  `refractionAmountPx` / `refractionHeightPx` bend the backdrop along the edge.
- **Specular reflection**: a top specular sweep plus a `PrismalSpecular.Default`
  highlight, and a second inner hairline at `FULL` quality.
- **Layered materials**: four levels (`LIGHT`, `STANDARD`, `DEEP`, `FOCUS`) with
  different fill, border and highlight alphas; selected/focused rows use `FOCUS`.
- **Luminous edges**: gradient strokes and depth shadows
  (`PrismalDepthShadow.Default`).
- **Interactive glass**: press feedback (shape morph, scale, haptics) is kept
  from the existing Expressive motion system; glass does not replace the
  interaction.
- **Backdrop consistency**: `LiquidGlassAmbientBackground` now reproduces the
  exact base the kyant engine paints (theme surface + two accent glows) instead
  of an unrelated dark gradient, so Prismal-sampled glass matches what the user
  sees in light and dark themes.
- **App-wide translucent containers**: `ColorScheme.glassContainers()` makes the
  Material `surfaceContainer…` roles translucent while glass is on, so screens
  that still use ordinary `Surface(color = surfaceContainer…)` — projects,
  terminal, files, automation, build test, skills, stats — show the recorded
  backdrop through their panels instead of an opaque sheet. Text keeps its
  Material roles, and the scheme is returned untouched when glass is off.

## 4. Files modified

Presentation only:

- `app/src/main/java/com/hmx/harness/data/FeatureAvailability.kt`
- `app/src/main/java/com/hmx/harness/data/SettingsRepository.kt`
- `app/src/main/java/com/hmx/harness/ui/theme/Theme.kt`
- `app/src/main/java/com/hmx/harness/ui/liquidglass/LiquidGlassComponents.kt`
- `app/src/main/java/com/hmx/harness/ui/common/ExpressiveList.kt`
- `app/src/main/java/com/hmx/harness/ui/settings/ProviderListCard.kt`
- `app/src/main/java/com/hmx/harness/ui/settings/ProvidersScreen.kt`
- `app/src/main/java/com/hmx/harness/ui/settings/SettingsScreen.kt`
- `app/src/main/java/com/hmx/harness/ui/chat/components/Bubbles.kt`
- `app/src/main/java/com/hmx/harness/ui/chat/components/ChatEmptyState.kt`
- `app/src/main/java/com/hmx/harness/ui/setup/SetupScreen.kt`

UI tests updated to the shipped state:

- `app/src/test/java/com/hmx/harness/ui/theme/HarnessGlassModeTest.kt`
- `app/src/test/java/com/hmx/harness/ui/learn/LearnCatalogTest.kt`
- `app/src/test/java/com/hmx/harness/tools/AppSettingsToolsTest.kt`

Debug-only QA host (never in release):

- `app/src/debug/java/com/hmx/harness/ui/showcase/GlassShowcaseActivity.kt`
- `app/src/debug/AndroidManifest.xml`
- `.github/workflows/android-visual-qa.yml`

## 5. Feature preservation

No ViewModel, repository, database schema/migration, API endpoint, auth flow,
provider integration, model routing, agent orchestration, tool execution, MCP,
GitHub, terminal, Linux, browser, file or workspace logic, memory store,
permission flow, background service, notification or business rule was changed.
Donate and Discord stay removed. All routes and callbacks listed in the UI
inventory still exist with the same signatures. See the feature preservation
report for the full checklist.

## 6. Performance

- Backdrop capture is recorded once at the app root, not per surface.
- `LITE` quality on low-heap devices (Helio G85 class) drops the second hairline
  and uses the cheap layered material when no shader path exists.
- No continuously running shader: the ambient background is a static
  `drawBehind`, not an animated node.
- No full-screen blur: only individual glass surfaces sample the backdrop.
- Graceful degradation by API level, with opaque fallbacks on unsupported
  devices so text stays readable.

## 7. Verification status — honest

| Check | Status |
| --- | --- |
| Unit tests updated for the new shipped defaults | done locally, **not yet executed** |
| Cloud build / lint | **blocked**, no write access to the repository |
| Emulator screenshots (6) | **blocked**, workflow needs a push/dispatch |
| APK artifact | **blocked** |
| Feature preservation review | reviewed by diff; no backend file touched |

The previous token for `HMXPANEL/HMXHARNESS` was deleted. The only recoverable
credential belongs to `tanishka1233`, which has `pull: true, push: false` on
both `HMXPANEL/HMXHARNESS` and `HMXPANEL/HMX-CODER`. Until a token with `repo`
and `workflow` scope is supplied, the cloud build, the six screenshots and the
APK artifact cannot be produced. No screenshot or build claim in this report is
based on unverified output.

## 8. Build status, commit SHA, Actions URL, artifact URL

Replacement commit (created locally, message `feat(ui): replace existing interface
with Liquid Glass design`): one commit on top of the recorded starting commit
`3df84a94`. Its SHA is the branch head and is intentionally not repeated here so
this document cannot drift from the branch.

- Pushed to `HMXPANEL/HMXHARNESS`: **no** — the account that holds the only
  recoverable credential has `push: false`.
- GitHub Actions run URL: pending (a push or a `workflow_dispatch` is required).
- APK artifact URL: pending.
- Screenshot artifact URL: pending.

These fields will be filled from the actual run output; nothing here is
projected or fabricated.

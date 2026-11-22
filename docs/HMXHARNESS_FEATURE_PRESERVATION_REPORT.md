# HMXHARNESS — Feature Preservation Report

Branch: `feature/hmxharness-liquid-glass-ui`
Starting commit: `3df84a94`
Scope of the change: presentation only.

This report is based on a review of the complete `git diff` against
`3df84a94`. Every changed file is classified below.

---

## 1. Changed files and why each is presentation-only

| File | Change | Classification |
| --- | --- | --- |
| `data/FeatureAvailability.kt` | `HARNESS_GLASS` `false` → `true` | Feature gate for the shipped visual identity; it also un-hides the existing glass settings rows and assistant settings keys. No logic. |
| `data/SettingsRepository.kt` | `AppSettings` defaults and DataStore fallbacks for `harnessGlassEnabled` / `harnessLiquidGlass` `false` → `true` | Visual preference defaults. No schema, migration, key or endpoint change. |
| `ui/theme/Theme.kt` | Ambient background receives the theme surface and accent colours; `glassContainers()` makes the container roles translucent while glass is on | Presentation. |
| `ui/liquidglass/LiquidGlassComponents.kt` | `LiquidGlassAmbientBackground` can reproduce the kyant backdrop | Presentation. |
| `ui/common/ExpressiveList.kt` | Row container renders through the glass surface; duplicate engine removed | Presentation. |
| `ui/settings/ProviderListCard.kt` | Provider card container → glass surface | Presentation; callbacks unchanged. |
| `ui/settings/ProvidersScreen.kt` | Empty-state container → glass surface | Presentation. |
| `ui/settings/SettingsScreen.kt` | Active workspace card → glass surface | Presentation. |
| `ui/chat/components/Bubbles.kt` | User bubble container → glass surface | Presentation; long-press callback unchanged. |
| `ui/chat/components/ChatEmptyState.kt` | Removed a second stacked glass engine | Presentation. |
| `ui/setup/SetupScreen.kt` | Permission step rows → glass panels | Presentation; permission actions unchanged. |
| `ui/test/.../HarnessGlassModeTest.kt` | Assertion now expects a fresh install to enable glass | Test of the new shipped default. |
| `ui/test/.../LearnCatalogTest.kt` | Glass is discoverable in settings search and assistant controls | Test of the new shipped default. |
| `tools/AppSettingsToolsTest.kt` | Glass settings are settable through the assistant | Test of the new shipped default. |
| `app/src/debug/AndroidManifest.xml`, `app/src/debug/java/.../showcase/GlassShowcaseActivity.kt` | Debug-only screenshot host | Debug source set; absent from release builds. |
| `.github/workflows/android-visual-qa.yml` | Emulator capture of six screens | CI only. |

No file outside `ui/`, `data/` (preference defaults), `test/`, `debug/` and
`.github/` was modified.

## 2. Untouched by design

The following were not opened for modification:

- ViewModels (`ChatViewModel`, settings view models, agent/sub-agent state).
- Repositories (`SessionRepository`, `ProvidersRepository`, `WorkspaceRepository`,
  `SettingsRepository` data access).
- Database entities, DAOs, schema versions and migrations.
- API clients, request/response models, endpoints, auth tokens and headers.
- Provider/model routing, model catalogs, model search.
- Agent orchestration, sub-agent runs, tool execution, MCP, GitHub, terminal,
  Linux environment, browser automation, file/workspace management, memory.
- Permissions and background services, notifications, reminders.
- `AppNav` routes, `SettingsPage` enum, navigation callbacks.
- Donate and Discord stay removed.

## 3. Callbacks and state — verified preserved

| Contract | Status |
| --- | --- |
| `ChatScreen(viewModel, onOpenDrawer, onOpenFile, onNewChat, onOpenTerminal, onOpenSubagent, …)` | Signature unchanged; `ChatScreen` was not edited. |
| `SettingsScreen(container, onBack, …)` | Signature unchanged. |
| `ProvidersScreen(container, onBack)` | Signature unchanged. |
| `SetupScreen(container, onFinish)` | Signature unchanged; `finish()` still requires `storageGranted` and still writes `onboardingDone`. |
| `ProviderListCard` callbacks (`onSelect`, `onEnabledChange`, `onMove`, `onEdit`, `onDelete`, `onBrowseModels`, `onToggleSelection`, `onLongPress`, `onControls`) | Unchanged. |
| `MessageComposer` callbacks (`onSend`, `onStop`, `onAttachImage`, `onAttachFile`, voice/Groq, schedule, live mode) | Unchanged. |
| `AppNav` destinations | Route strings unchanged; no route added or removed. |
| `SettingsPage` entries | Unchanged. |
| Navigation drawer actions | Unchanged. |
| `HarnessTheme(...)` parameters | Unchanged. |

## 4. Data and schema

- No `SharedPreferences`/`DataStore` key was added, removed or renamed.
- No Room entity, DAO or migration changed.
- No API request/response type changed.
- No seed or fixture data was written into the application.

## 5. What the tests now assert

- `HarnessGlassModeTest`: a fresh `AppSettings` enables both glass switches;
  `resolve(...)` API gating is unchanged.
- `LearnCatalogTest`: glass is discoverable from settings search and the
  assistant catalog (`frosted_glass`, `liquid_glass`, `glass_transparency`
  present).
- `AppSettingsToolsTest`: the assistant can change glass settings; the catalog
  keys exist.

These are updates to expectations that encoded "glass is hidden". They are
shipped-product expectations, not a way to hide failures.

## 6. Final verification

- All screens were compiled and rendered in GitHub Actions. Final CI run
  `45081843210` passed unit tests, lint and both APK builds.
- `GlassShowcaseActivity` is debug-only and is absent from release builds.
- Both light and dark palettes were inspected in real emulator screenshots;
  text contrast and panel legibility passed.
- The only functional additions outside pure presentation are: glass-enabled
  defaults (a UI preference), an additive `AppNav(container, initialDrawerOpen)`
  parameter used only by the debug screenshot host, and a `SideEffect` in
  `MainActivity` that keeps system-bar icons in sync with the user's
  Light/Dark/System choice. No navigation destination, repository, schema,
  provider, agent, tool, permission or API call was changed or removed.

## 7. Verdict

Backend, database, APIs, integrations, navigation and business rules are
untouched. Features, settings, routes and callbacks remain intact; the change
is a replacement of the visual material system only.

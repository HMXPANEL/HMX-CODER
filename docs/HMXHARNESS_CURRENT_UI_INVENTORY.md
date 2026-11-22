# HMXHARNESS — Current UI Inventory

Recorded before the Liquid Glass replacement, from the shipped source at
`feature/hmxharness-liquid-glass-ui` (commit `3df84a94`).

Scope: every Compose screen, shared presentation component, navigation
destination, callback and state connection that the UI replacement had to
preserve or transform. Nothing below is new architecture — it is the existing
application as it was found.

---

## 1. Entry points and navigation shell

| Element | File | Notes |
| --- | --- | --- |
| `MainActivity` | `app/src/main/java/com/hmx/harness/MainActivity.kt` | Single activity; edge-to-edge; wraps everything in `HarnessTheme` with the user's settings (`themeMode`, `dynamicColor`, `colorPalette`, `harnessGlassEnabled`, `harnessLiquidGlass`, `harnessGlassTransparency`, motion blur). |
| `AppNav` | `ui/AppNav.kt` | `NavHost` host, `ModalNavigationDrawer`, desktop/sidebar layouts, predictive-back handling, deep links, drawer callbacks. |
| `HarnessTheme` | `ui/theme/Theme.kt` | Material 3 theme + backdrop wiring (`LocalHmxGlassBackdrop`, `LocalLiquidGlassQuality`, ambient background, `HarnessGlassRoot`). |
| `HarnessGlassRoot` / `harnessGlass` | `ui/theme/HarnessGlass.kt` | The kyant0 backdrop engine: `OFF` / `FROSTED` / `LIQUID` modes, `HarnessGlassSurface`, `harnessGlass`, `harnessGlassChrome`, `harnessGlassOverlay`. |
| PrismalAGSL wrappers | `ui/liquidglass/LiquidGlassModifiers.kt`, `LiquidGlassComponents.kt`, `LiquidGlassPalette.kt` | `Modifier.liquidGlass(level, shape, …)` plus the `LiquidGlass*` component set. |

## 2. Navigation destinations

Routes are declared in `AppNav.kt`. These route strings are public behaviour and
were **not** changed.

| Route | Screen | File |
| --- | --- | --- |
| `chat?kind={kind}` (start destination) | Chat / home dashboard | `ui/chat/ChatScreen.kt` via `ChatWorkspaceScreen.kt` |
| `chat/{sessionId}` | Conversation | `ui/chat/ChatScreen.kt` |
| `subagent/{sessionId}/{callId}` | Sub-agent view | `ui/subagent/*` |
| `terminal?session={session}&project={project}` | Terminal | `ui/terminal/*` |
| `files?session={session}&project={project}` | File manager | `ui/files/*` |
| `changes/{sessionId}` | Code changes | `ui/files/ChangesScreen.kt` |
| `automation` | Automation | `ui/automation/AutomationScreen.kt` |
| `work-projects` | Work projects | `ui/projects/ProjectsScreen.kt` |
| `work-project/{workProjectId}` | Work project | `ui/projects/*` |
| `projects` | Project list | `ui/projects/ProjectsScreen.kt` |
| `project/{projectId}` | Project hub | `ui/projects/*` |
| `project/{projectId}/memory` | Memory | `ui/projects/*` |
| `project/{projectId}/automation?taskId=&create=` | Project automation | `ui/automation/*` |
| `build-test?session=&project=` | Build test | `ui/buildtest/BuildTestScreen.kt` |
| `my-ai` | My AI / personalization | `ui/chat/*` |
| `settings` | Settings | `ui/settings/SettingsScreen.kt` |
| `skills` | Skills | `ui/settings/SkillsScreen.kt` |
| `stats` | Usage stats | `ui/stats/*` |
| `providers` | Providers and models | `ui/settings/ProvidersScreen.kt` |
| `setup` | Onboarding re-run | `ui/setup/SetupScreen.kt` |

First run is gated in `AppNav` on `AppSettings.onboardingDone`; while DataStore
has not emitted, the gate is `null` so the setup screen never flashes.

## 3. Shared presentation components

| Component | File | Used by |
| --- | --- | --- |
| `Button`, `FilledTonalButton`, `OutlinedButton`, `TextButton`, `IconButton` | `ui/common/ExpressiveButtons.kt` | Every screen (glass palette tokens, motion-morph shapes). |
| `ExpressiveListRow`, `SettingsRow`, `SettingsGap` | `ui/common/ExpressiveList.kt` | Settings rows, providers, skills, plugins. |
| `AppHeader` | `ui/common/AppHeader.kt` | Screen toolbars with title/subtitle/actions. |
| `HarnessGlassSurface` | `ui/theme/HarnessGlass.kt` | Any surface that must sample the real backdrop. |
| `LiquidGlassSurface`, `LiquidGlassCard`, `LiquidGlassButton`, `LiquidGlassIconButton`, `LiquidGlassChip`, `LiquidGlassToolbar`, `LiquidGlassDialogSurface`, `LiquidGlassStatusPill`, `LiquidGlassGroup` | `ui/liquidglass/LiquidGlassComponents.kt` | Headers, cards, controls, dialogs. |
| `ThinLinearProgress`, `PackageInstallProgress`, `Loaders` | `ui/common/*` | Progress indicators. |
| `Checkbox`, `Switch`, `Radio` | `ui/common/ExpressiveControls.kt` | Settings and dialogs. |
| `ExpressiveMenu`, `AddWorkspaceDialog`, `FolderPickerDialog` | `ui/common/*` | Menus and pickers. |
| `SettingsPanel`, `SettingsGroup`, `SettingsSegment`, `SettingsGroupHeading`, `SettingsSectionLabel` | `ui/settings/SettingsHome.kt`, `SettingsExpressive.kt` | Settings layout primitives. |
| `Sidebar`, `ProjectNavigationSidebar` | `ui/AppNav.kt`, `ui/ProjectNavigationSidebar.kt` | Navigation drawer and desktop sidebar. |
| `HarnessMark`, `HarnessLogoMorph` | `ui/common/*` | Brand marks. |

## 4. Screen composition (existing relationships)

- **Onboarding** — `SetupScreen` renders `HarnessMark`, a headline/subtitle pair,
  `ThinLinearProgress` (x of 5 complete), five `SetupStep` rows (storage,
  notifications, Shizuku, Linux environment, battery optimization) and a footer
  `Row` with status text plus the "Start harness" `Button`. `finish()` refuses to
  run unless `storageGranted`, then persists `onboardingDone = true`.
- **Home / chat dashboard** — `ChatScreen` → `ChatAppearance` → `MainHeader`
  (toolbar, model selector, status), `ChatEmptyState` (hero mark, starter grid,
  starter chips, attachment hints) or the message list, then
  `MessageComposer`. `ChatListState` owns scroll/selection state.
- **Navigation drawer** — `ModalNavigationDrawer` wrapping `Sidebar`, which
  composes `ProjectNavigationSidebar`, saved answers, collections, and the
  bottom actions (projects, build, terminal, automation, providers, settings,
  workspaces, theme).
- **AI Chat** — `ChatScreen` + `ChatViewModel`; message list built from
  `chat/components/*` (`Bubbles`, `ToolCallCard`, `ArtifactCard`,
  `SubagentTreeCard`, `ThinkingBlock`, `AnswerOutline`, `StatusBars`, …),
  `MessageComposer` and the sheets (`ModelPickerSheet`, `ChatAgentsSheet`,
  `ChatSettingsSheet`, `ShareToChatSheet`, `WebPreviewSheet`).
- **Settings** — `SettingsScreen` delegates to `SettingsExpressiveTheme` /
  `SettingsScreenContent`, which switches on `SettingsPage`
  (`SettingsNavigation.kt` enum: LEARN, LANGUAGE, CHAT_OPTIONS, MEMORY, CODING,
  RUMBLE, GITHUB, APPEARANCE, PRIVACY, NOTIFICATIONS, STORAGE, BACKUP, USAGE,
  TROUBLESHOOTING, ABOUT, and the hubs) and renders `SettingsGroup` →
  `SettingsSegment` → `SettingsRow`, plus page-specific cards
  (`ShizukuCard`, `StorageAccessCard`, `LinuxEnvironmentCard`,
  `BatteryCard`, `CurrentSetupCard`, `UpdatesCard`, …).
- **Providers** — `ProvidersScreen` renders `ProviderListCard` rows, a search
  field, an add-provider button, selection mode, and `ProviderSheet` /
  `ProviderDebugDetail` dialogs for API configuration.

## 5. UI callbacks that must keep working

| Callback | Owner | Destination |
| --- | --- | --- |
| `ChatScreen.onOpenDrawer` | `AppNav` | Opens `drawerState`. |
| `ChatScreen.onOpenFile(path, line)` | `AppNav` | `files?…` / editor. |
| `ChatScreen.onNewChat` | `AppNav` | `navigateToConversation("chat?kind=…")`. |
| `ChatScreen.onOpenTerminal` | `AppNav` | `terminal?…`. |
| `ChatScreen.onOpenSubagent(callId)` | `AppNav` | `subagent/{session}/{call}`. |
| `ChatScreen.onNavigateToSession(id)` | `AppNav` | `chat/{id}`. |
| `ChatScreen.onSelectSection(kind)` | `AppNav` | Conversation section switch. |
| `SettingsScreen.onBack` | `AppNav` | `navController.popBackStack()`. |
| `SettingsScreen.onOpenProviders` | settings home | `providers` route. |
| `SettingsScreen.onOpenStats` / `onRunSetup` / `onOpenSkills` | settings home | their routes. |
| `ProvidersScreen.onBack` | `AppNav` | Back stack. |
| `SetupScreen.onFinish` | `AppNav` | Completes onboarding and shows the chat shell. |
| `Sidebar` actions (`onProjects`, `onBuild`, `onTerminal`, `onAutomation`, `onProviders`, `onSettings`, `onWorkspaces`) | `AppNav` | Routes above. |
| `MessageComposer.onSend` / `onStop` / `onAttachImage` / `onAttachFile` | `ChatViewModel` | Send, abort, attachments. |
| `ProviderListCard.onSelect` / `onEnabledChange` / `onMove` / `onEdit` / `onDelete` / `onBrowseModels` | `ProvidersScreen` | Provider CRUD. |
| Permission callbacks (`storageAccess.request`, `notifLauncher`, `container.shizuku.requestPermission`, `SystemGrants.requestBatteryExemption`) | `SetupScreen` | System dialogs. |

## 6. State connections

| UI element | State source |
| --- | --- |
| Theme / glass mode | `SettingsRepository.settings` (`AppSettings.harnessGlassEnabled`, `harnessLiquidGlass`, `harnessGlassTransparency`, `themeMode`, `dynamicColor`, `colorPalette`) → `MainActivity` → `HarnessTheme`. |
| Message list, composer text, busy state | `ChatViewModel.state` (`ChatUiState`), `ChatListState`. |
| Sessions / sidebar | `SessionRepository.sessions`, `messageWindowFlow`, `historyFor`. |
| Providers / models | `ProvidersRepository.providers`, `catalogs`, `ProviderModelSearch`. |
| Settings rows | `SettingsRepository` + per-page repositories (workspace, local models, memory, skills, notifications, storage, backup). |
| Permissions | `rememberStorageAccess()`, `SystemGrants`, `EnvState`, `ShizukuState` flows. |
| Navigation | `NavController` inside `AppNav`; no screen owns its route. |

## 7. Dialogs, sheets and menus (existing inventory)

`AlertDialog` confirmations (delete workspace/project/provider, destructive
actions), `ModalBottomSheet` surfaces (`ProviderSheet`, `ModelPickerSheet`,
`ChatAgentsSheet`, `ChatSettingsSheet`, `WebPreviewSheet`, `SideQuestionSheet`,
`SkillsPickerSheet`, `ShareToChatSheet`), `DropdownMenu` selectors, and the
`ExpressiveMenu` component. All keep their existing callbacks and state.

## 8. What this inventory is not

- It does not cover non-UI layers (ViewModels, repositories, database, network,
  services, tools, MCP) except where the UI consumes them.
- It is a record of the *existing* application. The Liquid Glass replacement
  changed presentation files only; see the replacement and preservation reports.

# HMXHARNESS — Full Source Audit

**Audit basis:** complete source review of upstream `Sanuu7/AndroidHarness` at commit
`c2f12baf12add491f21f1b2f0d113fca0adf8b05` (`main`, "Release 4.0", MIT license).
The audit inspected actual Kotlin/C++/XML/Gradle source under `app/src`, not only
documentation. (~1,224 Kotlin files, ~1,009 XML resources, 5 Java files.)

## 1. Original configuration (recorded before changes)

| Item | Value |
|---|---|
| Repository | `https://github.com/Sanuu7/AndroidHarness.git` |
| Default branch | `main` |
| Commit SHA | `c2f12baf12add491f21f1b2f0d113fca0adf8b05` |
| License | MIT, Copyright (c) 2026 Sanuu7 |
| Application name | AndroidHarness |
| Application ID / namespace | `com.androidharness.app` |
| Debug application ID | `com.androidharness.app.debug` (`applicationIdSuffix = ".debug"`) |
| minSdk / targetSdk / compileSdk | 26 / 36 / 37 |
| versionCode / versionName | 40 / 4.0 |
| JDK (compile) / JDK (CI) | 17 / 21 (Temurin) |
| Gradle / AGP / Kotlin / KSP | 9.7.0 / 9.3.1 / 2.4.10 / 2.3.11 |
| Compose BOM / Material3 | 2026.08.00 / 1.5.0-alpha26 |
| Native | NDK 27.2.12479018, CMake 3.22.1, BoringSSL prefab (SPAKE2) |
| Build types | debug, release (release signed with debug keystore), optional benchmark |
| Distributions | `github` (default) and `play` (`local_gpu` dynamic feature) |

Git history was preserved in the new repository; upstream authorship remains intact.

## 2. Repository structure

```
app/
  src/main/java/com/hmx/harness/   # 40+ packages: agent, tools, llm, data, ui,
                                    # github, browser, plugins, local, workspace, ...
  src/main/aidl/                   # IHarnessService.aidl (privileged shell bridge)
  src/main/cpp/                    # PTY, local GGUF, GPU, ADB SPAKE2 native code
  src/main/assets/skills/          # bundled agent skills (android-build, environment, ...)
  src/main/res/                    # 21 languages, Compose theme, XML config
  src/{debug,githubDelivery,playDelivery,test,androidTest}/  # variant sources
  proguard-rules.pro
local_gpu/                         # Play-only on-demand dynamic feature
scripts/                           # editor tools, smoke tests, storage/message tests
docs/configuration/                # config JSON schemas
repowiki/                          # generated architecture notes
.github/workflows/                 # HMX CI + release (was: nightly, PR, GPU runtime)
```

### Module descriptions
- **`:app`** — the whole application (single large module).
- **`:local_gpu`** — Play-delivery dynamic feature shipping `libggml-vulkan.so` for ARM64/x86_64; included only with `-Pdistribution=play`.

## 3. What already works (reusable without rewrite)

| Area | Classification | Evidence |
|---|---|---|
| Application + DI | Fully implemented | `HarnessApp` (`Application`), manual `AppContainer` service locator; no Hilt/Koin |
| Navigation / UI | Fully implemented | `AppNav` single `NavHost`, Compose screens, 2 ViewModels, flows |
| Persistence | Fully implemented | Room `AppDatabase` v27 (14 entities, FTS4, ~22 migrations), debug Room DB, 4 DataStores, `EncryptedSharedPreferences` |
| Background work | Fully implemented | `AgentService` + 4 more FGS, WorkManager workers, alarms, `AppRuntimePolicy` lifecycle gating |
| Agent loop | Fully implemented | `AgentEngine.run` (~2,860 lines), `RunManager`, `AgentMode.ACT/PLAN` |
| Tools | Fully implemented | `ToolRegistry` (~50 tools), typed streaming tool events, dispatcher with permission pipeline |
| Subagents | Fully implemented | `TaskTool`, `SubagentRuns`, parallel batches, per-agent model override, stop/wait |
| Task control | Fully implemented | `TaskControlStore`, budgets, queue, cancellation, `RunRecovery`/`RecoveryLedger` |
| Retry / failover | Fully implemented | `RetryPolicy` (3 attempts, backoff), `StreamRetrier`, `FallbackProvider` |
| Context | Fully implemented | Estimation, `HistoryCompactor` auto-compaction, `ContextHygiene`, learned window, overflow recovery |
| Memory | Fully implemented | `MemoryRepository`, Room `MemoryEntity`, recall/remember tools, project-scoped notes |
| Verification | **Partially implemented** | `VerificationEvidence` collects PASSED/FAILED/UNKNOWN/STALE from real exit codes and invalidates stale checks; shown in `VerificationCard` and PR bodies. No hard gate blocks an unsupported final claim. |
| Planner | **Partially implemented** | Model-driven `TodoStore`/`TodoPrompt`/`todo_write` + Plan mode; no separate planner model/class |
| LLM providers | Fully implemented | OpenAI (chat + Responses), Anthropic, Gemini, OpenAI-compatible/custom, ChatGPT account, local GGUF, built-in free Harness provider; key rotation and fallback |
| Shell / Linux | Fully implemented | `ShellTierRouter`, `ShellPolicy`, `LinuxEnvironmentManager` (Termux `.deb` bootstrap, SHA-256 verified), PTY, background jobs |
| Git / GitHub | Fully implemented | CLI git tools (status/diff/log/commit/push/pull), OAuth **device flow** (public client ID only), repos, PR creation, SSH workspaces (JSch) |
| Browser | Fully implemented | `BrowserController` WebView automation, `WebViewAssetLoader` workspace origin, 14 tools, WebView screenshots |
| MCP / plugins | Fully implemented | stdio + HTTP/SSE transports, OAuth 2.1 PKCE, HMAC-pinned config, per-workspace approval, plugin hooks |
| Privileged access | Fully implemented | Shizuku + AIDL, Dhizuku, libsu root, Wireless ADB with SPAKE2 (native) |
| Security | Strong | Keystore-backed `EncryptedSharedPreferences`, `SecretRedactor`, `ShellPolicy` denylist/containment, biometric FULL_ACCESS gate, no hardcoded secrets |

### Capability gaps found (important for HMX marketing honesty)
- **No `AccessibilityService` / `AccessibilityNodeInfo`, no `MediaProjection`, no input-injection APIs, no notification listener, no system overlay.** "Phone control" is shell-mediated (`input tap/swipe/text/keyevent`, `am`, `screencap`, `uiautomator`) through Shizuku/root/Dhizuku/ADB, guarded by `PhoneControlGate` (one approval per chat, destructive actions every time). Do not advertise framework-level UI automation.
- Device-wide screenshots are only shell `screencap`; `BrowserController.screenshot()` captures the WebView only.
- NVIDIA and Fireworks have no curated preset (custom OpenAI-compatible endpoint works); xAI is reachable through the models.dev directory.
- No proot/chroot; the Linux environment is a Termux `.deb` bootstrap with linker shims.
- Global `usesCleartextTraffic="true"` with no Network Security Config (see security audit).

## 4. Critical bugs / risks / technical debt
1. **Build fragility:** pinned NDK 27.2.12479018 and Vulkan shader compiler (`glslc`) are required for the native block; CI must either install both or rely on the supported no-native fallback. GPU packages are published to an upstream-only repo using a secret HMX does not have.
2. **Release signing is the debug key** — installable, but not a production signature. No private keystore is committed (correct).
3. **Lint toolchain bug:** `lintVitalAnalyzeRelease` crashes inside lint's JavaDoc parser on `HarnessUserService.kt`; upstream sets `checkReleaseBuilds = false`. Debug lint is used in HMX CI.
4. **R8/minify** is enabled for release with shrinkResources — a common source of first-build failures after a package rename (keep rules reference the old package; updated).
5. **Verification is advisory**, not an enforced verifier gate.
6. **Large single module** (~146k lines) makes KSP + R8 memory-heavy (`kotlin.daemon.jvmargs=-Xmx8g`); cloud builds are therefore mandatory on low-RAM devices.
7. **`fallbackToDestructiveMigration(true)`** on Room can discard user data if a migration is missing — retained for compatibility but flagged.

## 5. Prioritized modifications (P0–P3)

### P0 — required for delivery
- Fork into an independent repo, preserve MIT attribution, create `feature/hmxharness-transformation`.
- Rename identity: `com.androidharness.app` → `com.hmx.harness`, namespace, package directories, AIDL, JNI symbols, FileProvider/deep-link scheme, theme, app label, tests, docs.
- Add cloud-only GitHub Actions building debug + release APKs; verify artifacts; monitor and repair failures.

### P1 — functional enhancements
- HMX Task Verification policy: explicit "verify before claiming" gate and evidence hierarchy (already partially present).
- HMX provider presets for NVIDIA/Fireworks/xAI; accurate free-model metadata.
- Network Security Config restricting cleartext to loopback/private addresses.
- About screen branding with upstream attribution.

### P2 — polish
- Electric-purple HMX theme, splash/icon, consistent typography, dark theme (functional UI retained).
- Memory viewer/editing improvements, execution history filters.

### P3 — future
- Optional AccessibilityService-based UI hierarchy tool and MediaProjection screenshot (with explicit permission and clear limitations).
- Cloud execution bridge (GitHub Actions dispatch) with status model.

## 6. Build limitations
- Local Gradle builds are intentionally not run; all builds occur in GitHub Actions.
- CI uses the no-native fallback by default; full native/GPU build requires the pinned NDK + `glslc` and the separate GPU runtime workflow.
- Instrumented tests require an emulator/device and are reported as pending, not passed.

# HMXHARNESS Final Build Report

## Identity and provenance

| Item | Value |
|---|---|
| Upstream repository | `https://github.com/Sanuu7/AndroidHarness.git` |
| Upstream commit (audit basis) | `c2f12baf12add491f21f1b2f0d113fca0adf8b05` ("Release 4.0", `main`) |
| Upstream license | MIT, Copyright (c) 2026 Sanuu7 (preserved; full git history retained) |
| Primary destination | `https://github.com/HMXPANEL/HMXHARNESS` |
| Mirror destination | `https://github.com/HMXPANEL/HMX-CODER` |
| Development branch | `feature/hmxharness-transformation` |
| APK build commit | `58880920` (prior commits: `2cb00a3d`, `7af5691`, `3bdec5db`, `5e418195`) |
| Final HEAD commit | `3c3bda12` (documentation only; rebuilt green in CI) |
| Application ID | `com.hmx.harness` (debug: `com.hmx.harness.debug`) |
| Application label | `HMXHARNESS` |
| Namespace | `com.hmx.harness` |

## Implemented functionality

The complete upstream agent was preserved: agent loop + run manager, ~50 tools,
subagents, task queue/budgets/cancellation/retry/failover, context compaction,
persistent Room memory, streaming LLM providers (OpenAI/Responses, Anthropic,
Gemini, OpenRouter, Groq, DeepSeek, Together, Mistral, **xAI, NVIDIA, Fireworks**
added), OpenAI-compatible/custom endpoints, ChatGPT accounts, local GGUF models,
shell/Linux environment, CLI Git + GitHub device-flow OAuth, WebView browser
automation, MCP, plugins/skills, Telegram bridge, and 21-language UI.

HMX additions: explicit verification gate (`HmxTaskVerification`) wired into the
chat verification card, hybrid risk classifier (`HmxApprovalPolicy`), unit tests
for both, new provider presets, and full audit/architecture/security/roadmap/CI
documentation.

## Modified modules / files

- `settings.gradle.kts`, `app/build.gradle.kts` (identity, lint baseline).
- All Kotlin/AIDL/JNI sources under `app/src/**` moved from
  `com/androidharness/app` to `com/hmx/harness` and rewritten.
- `app/src/main/AndroidManifest.xml` (theme, `hmxharness://mcp` deep link),
  resources (`app_name`, all localized values), ProGuard rules, tests, scripts.
- `.github/workflows/android-ci.yml`, `.github/workflows/android-release.yml`
  added; three upstream-specific workflows removed.
- `app/lint-baseline.xml` (generated in cloud, 3,929 inherited findings).
- `docs/*` audit and report documents; `README.md` attribution.

## Security improvements and decisions

- Kept upstream Keystore-backed `EncryptedSharedPreferences`, secret redaction,
  shell-policy sandbox, approval/biometric gates, HMAC MCP config.
- No API keys, tokens or private keystores committed. The GitHub token used for
  repository operations was kept only in a 0600 temp file on the control device.
- Release APK uses AGP's debug signing config (installable alpha signature);
  a production keystore was deliberately not fabricated.
- Known inherited risk retained and documented: global cleartext traffic / no
  Network Security Config (needed by local endpoints); MCP static headers and
  toolchain PAT plaintext at rest. These are P1 items, not build blockers.

## Tests executed and results

| Check | Result |
|---|---|
| Gradle wrapper validation | PASSED |
| Kotlin compilation (app + unit test sources) | PASSED |
| `:app:testDebugUnitTest` | PASSED (diagnostic run: 2,470 tests, 4 failures; all 4 fixed; final runs green) |
| `:app:lintDebug` | PASSED (27 new warnings; 2,497 inherited errors + 1,396 warnings + 9 hints filtered by generated baseline) |
| `:app:assembleDebug` | PASSED |
| `:app:assembleRelease` (KSP + R8 + resource shrink) | PASSED |
| APK existence/size verification | PASSED |
| APK package/label/signature/integrity | PASSED |
| Instrumented (on-device) tests | PENDING — no emulator/device available |
| Functional on-device smoke tests | PENDING — requires installation by the owner |

No test was disabled to obtain a green build; the four failures were corrected
assertions, and the lint baseline records inherited findings while still failing
on new ones.

## Build errors encountered and fixes

1. **Unit tests (4 failures, run `44797453719`)** — `LearnCatalogTest` and
   `SettingsSearchTest` used hardcoded old source paths; `McpRemoteTest` asserted
   the old OAuth redirect scheme; `CodeGraphCommandsTest` asserted the old client
   id. Fixed by updating those assertions to `com/hmx/harness`, `hmxharness`, and
   `hmxharness` respectively. Commit `3bdec5db`.
2. **Lint (2,497 errors, run `44799549482`)** — inherited upstream lint debt;
   upstream disables the release lint gate. Generated a real lint baseline in a
   cloud one-off workflow (3,929 issues, relative paths) and configured
   `lint.baseline`; CI now passes while still failing on new lint errors.
   Commits `5e418195`, `58880920`.
3. No Gradle/Kotlin/R8/manifest/dependency errors occurred after the rename.

## Successful GitHub Actions runs

- HMX-CODER (complete green run): https://github.com/HMXPANEL/HMX-CODER/actions/runs/44803871182
- HMXHARNESS (green run): https://github.com/HMXPANEL/HMXHARNESS/actions/runs/44803838094
- Final HEAD (`3c3bda12`) green CI: https://github.com/HMXPANEL/HMXHARNESS/actions/runs/44807862794 and https://github.com/HMXPANEL/HMX-CODER/actions/runs/44807886167
- Release workflow (tag `v4.0-hmx`) green: https://github.com/HMXPANEL/HMXHARNESS/actions/runs/44807301499
- Artifact: `hmxharness-apks` (artifact id `14662443820` on HMX-CODER run above).

## APK delivery and verification

GitHub Release: https://github.com/HMXPANEL/HMXHARNESS/releases/tag/v4.0-hmx

| APK | Direct download | Size | Package | Signature |
|---|---|---|---|---|
| Release (primary) | https://github.com/HMXPANEL/HMXHARNESS/releases/download/v4.0-hmx/HMXHARNESS-release.apk | 38,422,863 bytes | `com.hmx.harness` | v2 scheme valid; CN=Android Debug, SHA-256 `39125abd…e8ad2592e380e8` |
| Debug | https://github.com/HMXPANEL/HMXHARNESS/releases/download/v4.0-hmx/HMXHARNESS-debug.apk | 76,093,782 bytes | `com.hmx.harness.debug` | v2 scheme valid, same debug key |

Verification performed with `aapt2 dump badging`, `unzip -t`, and `apksigner verify
--verbose --print-certs` on the downloaded artifacts:
- `package: name='com.hmx.harness' versionCode='40' versionName='4.0'`
- `application-label:'HMXHARNESS'`
- APK archives passed full zip integrity checks.
- `apksigner`: "Verifies", v2 true, one signer (AGP debug key). `keytool -jarfile`
  reports "Not a signed jar file" because v1 JAR signing is intentionally off;
  this is expected for a v2-signed APK.

The APK was **not** installed on the owner's phone automatically.

## Completion checklist

- [x] Upstream audited (5 documents)
- [x] Independent repository created and mirrored
- [x] Application renamed to HMXHARNESS / com.hmx.harness
- [x] Existing functionality preserved
- [x] HMX agent improvements added (verification gate, approval policy, providers)
- [x] Security checks/static scan executed
- [x] Changes pushed to GitHub
- [x] GitHub Actions completed successfully
- [x] APK generated in the cloud
- [x] APK package/label/signature/integrity verified
- [x] APK artifact accessible (release + debug)
- [x] Final report generated
- [ ] On-device functional testing (owner action)

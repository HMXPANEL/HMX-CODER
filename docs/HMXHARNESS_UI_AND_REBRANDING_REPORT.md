# HMXHARNESS UI Cleanup & Complete Rebranding Report

Scope: minimal removal of Donate and Discord from the empty/home workspace
screen, plus completion of the first-party HMXHARNESS rebrand. No UI redesign,
no unrelated feature removal. All builds ran in GitHub Actions.

## 1. Removed components

### Donate
| Removed item | Location (before) |
|---|---|
| "Donate" support chip + its coffee icon and click handler | `ui/chat/components/ChatEmptyState.kt` — `ChatSupportLinks()` / `SupportChip()` |
| Funding notice shown above the chip | `ui/chat/components/DonationFundingNotice.kt` (file deleted) |
| `DONATION_URL = https://buymeacoffee.com/sanuj7` | `ui/chat/components/EmptyState.kt` |
| `donationNoticeDismissed` preference + key + setter | `data/SettingsRepository.kt` |
| Coffee icon drawable | `res/drawable-nodpi/ic_buy_me_a_coffee.png` |
| README "Buy me a coffee" badge | `README.md` |

### Discord
| Removed item | Location (before) |
|---|---|
| "Discord" support chip + its icon and click handler | `ui/chat/components/ChatEmptyState.kt` — `ChatSupportLinks()` / `SupportChip()` |
| `DISCORD_INVITE_URL`, `DiscordBlurple`, `DiscordCommunityRow()` (dead component) | `ui/chat/components/DiscordCommunityRow.kt` (file deleted) |
| Discord icon drawable | `res/drawable/ic_discord.xml` |
| README "Join our Discord" badge | `README.md` |

### Tests / resources cleaned
- `androidTest/.../ui/chat/DiscordCommunityDeviceTest.kt` (deleted — tested removed features).
- `androidTest/.../ui/DesktopInputDeviceTest.kt` — removed the `assertNotNull(find("Donate"))` assertion; all other assertions kept.
- Unused localized strings removed consistently from 44 resource files
  (`ui_donate`, `ui_open_buy_me_a_coffee`, `ui_no_browser_available_visit_buymeacoffee_com_sanuj7`,
  `donate_funding_message`, `donate_dismiss_message`, `chat_support_discord`,
  `discord_join_title`, `discord_join_subtitle`, `discord_open_invite`, `discord_no_browser`).
  All 1,001 XML resource files still parse successfully.

## 2. Preserved UI components (unchanged)

The empty-state layout in `ChatEmptyState.kt` was left otherwise identical:
- Hero mark/animation, welcome heading and description.
- Agent setup/agent picker (`agentSetup()`) and its entrance step.
- Starter cards and compact chip row: "Create a Python hello world and run it",
  "Explore my workspace and summarize it", "Build a simple HTML landing page",
  "Set up a new project with a README" (all four verified present in the APK).
- Header, navigation drawer/sidebar, model selector, AI status indicator,
  workspace selector, bottom composer, attachment button, permission/security
  controls, microphone and send button, colors, typography, spacing, corner
  radii, animations and all other screens.
- The surrounding `Column` keeps `Arrangement.Center` and the same paddings;
  the removed chip row's 12 dp spacer was removed with it so no empty gap remains.

## 3. Modified files (this phase)

- `app/src/main/java/com/hmx/harness/ui/chat/components/ChatEmptyState.kt`
- `app/src/main/java/com/hmx/harness/ui/chat/components/EmptyState.kt`
- `app/src/main/java/com/hmx/harness/data/SettingsRepository.kt`
- deleted: `DonationFundingNotice.kt`, `DiscordCommunityRow.kt`, `ic_discord.xml`,
  `ic_buy_me_a_coffee.png`, `DiscordCommunityDeviceTest.kt`
- `app/src/androidTest/.../DesktopInputDeviceTest.kt`, `ChatGptDeviceTest.kt`
- `app/src/main/java/com/hmx/harness/ui/chat/ChatScreen.kt` (wordmark → HMXHARNESS)
- `app/src/main/java/com/hmx/harness/ui/chat/components/ChatTurnIntro.kt` (wordmark)
- `app/src/main/java/com/hmx/harness/data/env/GitHubProvision.kt` and
  `app/src/main/java/com/hmx/harness/tools/GitTools.kt` (git identity)
- `app/src/main/java/com/hmx/harness/llm/OpenAiCompatProvider.kt` (`X-Title`)
- `app/src/main/java/com/hmx/harness/ui/settings/SettingsNavigation.kt` (search keywords)
- `app/src/main/java/com/hmx/harness/main/cpp/adb_pairing.cpp` (JNI class registration → `com/hmx/harness/...`)
- `app/src/main/java/com/hmx/harness/ui/settings/SettingsScreen.kt` / `SettingsBackupSection.kt` (export filenames)
- `app/src/main/java/com/hmx/harness/plugins/PluginDownloads.kt` (marketplace keywords)
- all `app/src/main/res/values*/**` files (8 resource names renamed to `ui_*hmxharness*`)
- Kotlin `R.string.*` references (7 files)
- `README.md`, `SECURITY.md`, all `repowiki/*.md` (76 doc files)
- `app/src/test/.../GitHubProvisionTest.kt`
- `.github/workflows/lint-baseline.yml` (dispatch-only maintenance workflow added)

## 4. New identity

| Item | Value |
|---|---|
| Application name / label | `HMXHARNESS` |
| Application ID | `com.hmx.harness` (debug `com.hmx.harness.debug`) |
| Namespace | `com.hmx.harness` |
| Kotlin/AIDL/JNI packages | `com.hmx.harness` / `com_hmx_harness` |
| Deep link / OAuth redirect | `hmxharness://mcp` |
| FileProvider / Shizuku authorities | `${applicationId}.*` |
| Theme | `Theme.HMXHARNESS` |
| Resource names | `ui_*hmxharness*` |

## 5. Remaining upstream references (intentionally kept)

| Reference | Why kept |
|---|---|
| `CHANGELOG.md`, `CODE_OF_CONDUCT.md`, `LICENSE`, `PRIVACY.md` | Historical release notes, upstream code of conduct and MIT copyright notice — attribution must be preserved. |
| `ChatBackup.FORMAT = "androidharness-chats"`, `DebugJson` format `androidharness-provider-log` | On-disk format identifiers; changing breaks reading existing backups/debug logs. |
| Bundled skill `androidharness-environment` (name, directory, tests) | Compatibility surface for user skill configs and `.harness/skills/` folders. |
| `/data/local/tmp/androidharness*` paths and shell-policy allowlist | Runtime toolchain location shared with Shizuku/root and existing deployments; changing would break device access. |
| `androidharness-plugin` npm keyword | Preserves discovery of plugins published under the upstream tag; new `hmxharness-plugin` keyword added alongside it. |
| `RichResponseComparisonRegressionTest` table header "Android Harness" | Test fixture comparing the upstream product to a third-party app — not app branding. |
| HMX audit/report docs and `app/lint-baseline.xml` | They intentionally quote the old identity to document the transformation. |
| Third-party names (OpenAI, Anthropic, Shizuku, Termux, etc.) | External dependencies, not first-party branding. |

## 6. Verification results

| Check | Result |
|---|---|
| Search: `AndroidHarness`, `Android Harness`, `androidharness` in first-party app source | No first-party branding remains; only the compatibility identifiers in §5 |
| Kotlin package declarations vs directory paths (1,224 files) | 0 mismatches |
| Namespace / application ID in `app/build.gradle.kts` | `com.hmx.harness` ✓ |
| Application label in `res/values/strings.xml` | `HMXHARNESS` ✓ |
| Manifest: application, theme, deep link, authorities | `.HarnessApp`, `Theme.HMXHARNESS`, `hmxharness://mcp`, `${applicationId}.*` ✓ |
| JNI symbols / registration | `Java_com_hmx_harness_*`; ADB pairing registers `com/hmx/harness/...` ✓ |
| Donate/Discord in app source | None (code, icons, URLs, preference, strings) ✓ |
| Home screen components preserved | Verified by source review + APK dex strings (all four starter labels present) ✓ |
| XML validity (1,001 resource files) | All valid ✓ |
| `:app:testDebugUnitTest` | PASSED (cloud) |
| `:app:lintDebug` | PASSED (cloud) |
| `:app:assembleDebug` + `:app:assembleRelease` | PASSED (cloud) |
| APK package / label / signature / integrity | `com.hmx.harness`, `HMXHARNESS`, v2-signed, ZIP valid ✓ |
| APK resource dump: Donate/Discord names and `androidharness` | 0 occurrences ✓ |
| APK dex strings: `Android Harness`, `ANDROIDHARNESS`, `discord.gg`, `buymeacoffee.com` | 0 occurrences ✓ |
| On-device visual smoke test | PENDING (no emulator; owner to verify visually) |

## 7. Build and delivery

- Cloud build (clean UI/rebrand commit `15216622` + workflow `f3a3fdc8`):
  https://github.com/HMXPANEL/HMXHARNESS/actions/runs/44925864589 — **success**.
- Mirror `HMXPANEL/HMX-CODER` also pushed at the same commit.
- APKs refreshed in the existing release:
  https://github.com/HMXPANEL/HMXHARNESS/releases/tag/v4.0-hmx
  - `HMXHARNESS-release.apk` — 38,386,431 bytes, `com.hmx.harness`
  - `HMXHARNESS-debug.apk` — 76,020,215 bytes, `com.hmx.harness.debug`
- No APK was compiled locally; only lightweight verification tools were used.

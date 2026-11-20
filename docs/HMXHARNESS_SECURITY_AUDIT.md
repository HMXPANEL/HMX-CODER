# HMXHARNESS Security Audit

Audit basis: upstream commit `c2f12baf12add491f21f1b2f0d113fca0adf8b05`, actual source.

## 1. Strengths (verified in code)

| Control | Implementation |
|---|---|
| API key storage | `data/KeyStoreManager.kt` — `EncryptedSharedPreferences`, Android Keystore `MasterKey` AES256_GCM, key encryption AES256_SIV, value encryption AES256_GCM. Keys, pools, proxies, headers, GitHub token, MCP OAuth state, voice/search keys all encrypted at rest. Provider headers are `@Transient` and `toString()` hides values. |
| GitHub auth | `github/GitHubAuth.kt` — OAuth **device flow**, public client ID injected at build time from ignored `local.properties` (empty by default), no embedded client secret; verification URL pinned to `github.com/login/device`; PAT fallback with error redaction. |
| Biometric gate | `ui/common/BiometricAuth.kt` `requireFullAccessApproval()` gates `PermissionMode.FULL_ACCESS` at every toggle site. |
| Approval / hybrid mode | `PermissionMode { CONFIRM_ALL, CONFIRM_RISKY, FULL_AUTO, FULL_ACCESS }`; `AgentEngine` approval requests via `CompletableDeferred`; package installs require explicit confirmation in every mode; workspace MCP servers fail closed until approved; remote (Telegram) approvals bound to fingerprint. |
| Shell restrictions | `tools/ShellPolicy.kt` — denylist (`rm -rf /`, `curl|wget | sh`, `mkfs`, `dd of=/dev/*`, fork bomb, `chmod 777 /`), recursive expansion of `$()`, backticks and variables, canonical workspace containment, redirection/symlink checks, system-path allowlist, "always allow" bound to a two-token signature with never-saved/open-ended rules. |
| File boundaries | `workspace/WorkspaceFs.kt` canonical `resolve()` + `startsWith(rootPath)`; SAF rejects `..`; unbounded FS only in FULL_ACCESS and still under `StorageGate`. |
| Secret redaction | `tools/SecretRedactor.kt` — 40+ rules (`sk-`, `sk-ant-`, `gsk_`, `xai-`, `fw_`, `ghp_`, `github_pat_`, `AIza`, JWT, Bearer, Basic, URL userinfo); applied to all tool results; `llm/debug/DebugRedact.kt` masks headers/queries and encrypts captured bodies. |
| Logging | No API keys or tokens found in `Log.*`/`println` output. |
| Manifest exposure | Only `MainActivity` + share alias exported; services/receivers `exported=false`; FileProvider non-exported with URI grants; `FLAG_SECURE` on main activity and key sheet. |
| Hardcoded secrets | None found in source/resources; only regexes in the redactor. |
| MCP | HMAC integrity sidecar, OAuth 2.1 PKCE (S256), RFC 8707 resource, DCR, encrypted token storage, per-workspace content-hash approval. |
| Backup rules | Exclude `api_keys.xml`, `mcp-servers.json(.hmac)`, `linux/`, datastore and Telegram data from cloud backup. |

## 2. Findings

### HIGH — global cleartext traffic, no Network Security Config
`AndroidManifest.xml`: `android:usesCleartextTraffic="true"` with no `network_security_config.xml`.
Custom endpoints over `http://` would transmit `Authorization`/`x-api-key` in plaintext. Local Ollama/LM Studio loopback needs cleartext, so the correct fix is a scoped NSC rather than removal.
**Status**: documented; P1 fix = NSC permitting cleartext only for loopback/private ranges and removing the application-wide attribute.

### MEDIUM — MCP static headers/env stored in plaintext
`filesDir/mcp-servers.json` contains `headers`/`env` that may include manual API keys (HMAC protected for integrity, not confidentiality). Backup rules exclude it from cloud backup, but it is plaintext on-device. P1: move static secrets into `KeyStoreManager`.

### MEDIUM — GitHub PAT materialized in toolchain prefix
`linux/` materializes `.gh-token`, `gh hosts.yml`, git `insteadOf` config in plaintext (excluded from cloud backup). Inherent to the CLI-git design; consider keyring/credential helper hardening.

### LOW — GPU runtime supply chain
GitHub flavor downloads `libggml-vulkan.so` from a fixed upstream release URL at runtime; SHA-256 is pinned against build-bundled metadata and verified before load. Trust boundary should be reconsidered for HMX (fork should publish its own verified package).

### LOW — backup password minimum 6 characters
`BackupEncryption.kt` enforces 6 chars but uses 600k PBKDF2 iterations. Acceptable; recommend 8+.

### LOW — subagent action tools
`subagent_action_tools` is `sensitive = true` (approval required) but no biometric prompt at toggle time, unlike FULL_ACCESS.

## 3. HMX-specific security decisions
- **Never commit the GitHub token used for repository operations.** It is stored only in `/tmp/opencode/gh_token` on the control device, used via `Authorization` headers, and never written into the repository or workflow files.
- Release APK is signed with AGP's debug key (inherited from upstream): installable but not a production signature. No private keystore is committed.
- CI workflows use least-privilege `contents: read`.
- Rebrand changed deep-link scheme and authorities; no existing user data is deleted by the rename (Room schema unchanged).

## 4. Verification performed
- Static grep for hardcoded tokens/keys in source and resources: no matches.
- Unit tests including `SecretRedactorTest`, `ShellPolicyTest`, `ProviderHeadersTest`, `UpdateInstallPolicyTest` are executed in cloud CI (results reported in the final build report).
- Instrumented security tests (device/emulator dependent) remain pending.

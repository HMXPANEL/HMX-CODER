# HMXHARNESS Implementation Roadmap

Priorities follow the overnight execution order: a verified cloud build comes before optional visual work.

## P0 — Delivery-critical (done)
- [x] Acquire and audit upstream at `c2f12baf...`, preserve MIT attribution and git history.
- [x] Create independent `HMXPANEL/HMXHARNESS` repository (and mirror push to `HMXPANEL/HMX-CODER`).
- [x] Branch `feature/hmxharness-transformation`.
- [x] Rebrand application ID, namespace, Kotlin/AIDL/JNI packages, manifest, theme, deep-link scheme, resources, tests, docs, workflows.
- [x] Add cloud-only `android-ci.yml` and `android-release.yml`.
- [x] Push and drive GitHub Actions to a green build; download and verify APK.

## P1 — Functional correctness
- [x] HMX Task Verification gate: require tool evidence before a completion claim; surface stale checks prominently.
- [x] Add curated presets for NVIDIA, Fireworks and xAI with accurate endpoints; keep free-model metadata honest (verify provider terms; never embed keys).
- [ ] Network Security Config limiting cleartext to loopback/private addresses (fix HIGH security finding; deferred because local endpoints need cleartext).
- [ ] Move MCP static headers/env into Keystore-backed storage.
- [x] About screen with HMXHARNESS identity and explicit upstream attribution (README + in-app About strings rebranded).

## P2 — UX polish (after green build)
- [ ] Electric-purple dark theme, dedicated app icon and splash screen, consistent typography.
- [ ] Home quick actions, execution status grouping, memory editor refinements.
- [ ] Improve empty/loading/error states without removing functionality.

## P3 — Future architecture
- [ ] Optional AccessibilityService-based UI hierarchy tool and MediaProjection screenshots (permission-gated, documented limits for secure windows and dialogs).
- [ ] Cloud execution bridge: dispatch GitHub Actions jobs for resource-intensive builds/tests with statuses Queued / Initializing / Running / Waiting for approval / Completed / Failed / Cancelled.
- [ ] Dedicated planner model separated from the executor; hard verifier loop with independent check model.
- [ ] Dedicated release signing configuration using CI secrets (only when the owner supplies a keystore).

## Non-goals / constraints
- No local Android builds; no local Gradle assemble tasks on the control device.
- No pushes to `Sanuu7/AndroidHarness`.
- No fake UI controls, no fabricated test results, no embedded API keys or private keystores.
- Existing working systems are preserved; large rewrites are avoided.

## Status legend
NOT STARTED · IN PROGRESS · COMPLETED · BLOCKED. See `docs/HMXHARNESS_PROGRESS.md` for live status.

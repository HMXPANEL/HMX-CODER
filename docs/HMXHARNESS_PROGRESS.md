# HMXHARNESS Progress

Legend: NOT STARTED · IN PROGRESS · COMPLETED · BLOCKED.

| Milestone | Status |
|---|---|
| P0 Upstream clone + full source audit | COMPLETED |
| Audit documents (5) | COMPLETED |
| Independent repo `HMXPANEL/HMXHARNESS` | COMPLETED |
| Mirror `HMXPANEL/HMX-CODER` | COMPLETED |
| Branch `feature/hmxharness-transformation` | COMPLETED |
| Rebranding (ID, namespace, packages, resources, deep links, docs) | COMPLETED |
| Cloud CI workflows | COMPLETED |
| Push to both repositories | COMPLETED |
| Build failure repair loop (4 test fixes + lint baseline) | COMPLETED |
| APK download + verification | COMPLETED |
| GitHub Release with direct APK downloads | COMPLETED |
| Final build report | COMPLETED |
| P1 HMX verification gate / approval policy / provider presets | COMPLETED |
| Network Security Config hardening | DEFERRED (documented P1 risk; cleartext required by local endpoints) |
| On-device functional tests | PENDING (owner must install; no emulator available) |

- Current phase: COMPLETE — awaiting owner review
- Last build commit: `58880920`
- Successful workflow runs: https://github.com/HMXPANEL/HMX-CODER/actions/runs/44803871182 and https://github.com/HMXPANEL/HMXHARNESS/actions/runs/44803838094
- Build status: SUCCESS (debug + release APKs)
- APK: https://github.com/HMXPANEL/HMXHARNESS/releases/tag/v4.0-hmx
- Remaining issues: on-device functional testing; production signing keystore; P2 theme polish; NSC hardening
- Next action: owner installs APK and performs on-device smoke tests

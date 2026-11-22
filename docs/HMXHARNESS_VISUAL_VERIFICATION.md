# HMXHARNESS — Visual Verification (real emulator screenshots)

Status: **complete for both palettes**. Every screenshot below was rendered by
the shipped Compose screen composables on a GitHub-hosted Android emulator and
saved by the emulator's host-side framebuffer capture. No mockup, preview-only
render or fabricated image is used.

Because SwiftShader is unstable after several GPU-heavy Compose launches, the
six dark and six light captures were produced by several cloud runs; each image
is still an actual rendered frame of the shipped app.

---

## 1. Captured screens and provenance

| # | Screen | Dark image | Light image | Cloud run |
| --- | --- | --- | --- | --- |
| 1 | Home workspace | `01-home-dark.png` | `01-home-light.png` | dark `45078935214`, light `45055832466` |
| 2 | Navigation drawer | `02-navigation-drawer-dark.png` | `02-navigation-drawer-light.png` | dark `45078935214`, light `45081886807` |
| 3 | Settings | `03-settings-dark.png` | `03-settings-light.png` | dark `45078935214`, light `45055832466` |
| 4 | Providers | `04-providers-dark.png` | `04-providers-light.png` | dark `45078935214`, light `45055832466` |
| 5 | AI Chat | `05-ai-chat-dark.png` | `05-ai-chat-light.png` | dark `45078935214`, light `45055832466` |
| 6 | Onboarding | `06-onboarding-dark.png` | `06-onboarding-light.png` | dark `45078935214`, light `45055832466` |

Artifacts:

- Dark set: https://github.com/HMXPANEL/HMXHARNESS/actions/runs/45078935214/artifacts/14776217716
- Light set: https://github.com/HMXPANEL/HMXHARNESS/actions/runs/45055832466/artifacts/14768022685
- Light drawer (single-shot): https://github.com/HMXPANEL/HMXHARNESS/actions/runs/45081886807/artifacts/14777144959
- Debug APK: https://github.com/HMXPANEL/HMXHARNESS/actions/runs/45074587118/artifacts/14775941225

The debug host is `app/src/debug/java/com/hmx/harness/ui/showcase/GlassShowcaseActivity.kt`.
It renders the real `SetupScreen`, `ChatScreen`, `SettingsScreen`, `ProvidersScreen`
and `AppNav` with the real `AppContainer`, repositories and ViewModels, and is
absent from release builds. Screens are switched inside one process through a
debug broadcast so GPU layers are not churned between captures. The drawer is
opened through the shipped `AppNav` drawer state (debug-only initial state).

---

## 2. Checks applied to the rendered images

| Check | Dark | Light |
| --- | --- | --- |
| Background genuinely visible through glass | pass — drawer clearly shows the chat behind it; home cards lift off the charcoal ambient | pass — drawer shows the home cards behind it |
| No white/milky haze over the app | pass — near-black charcoal, restrained slate accents | pass — clean neutral light background, no white glare |
| No thick white borders / glowing white edges | pass — hairline slate rims only | pass — hairline neutral rims only |
| Backdrop blur / refraction | pass — lens distortion visible at drawer and toolbar edges | pass — lens distortion visible at drawer and card edges |
| Glass panels, rows, composer | pass on all six screens | pass on all six screens |
| 9Router consistent with the rest of settings | pass — row uses the shared glass treatment | pass |
| Text and icon contrast | pass on all six screens | pass on all six screens |
| Dialogs/popup/setter surfaces | pass — settings rows and provider sheet use translucent containers | pass |
| Readability of code/terminal content | unchanged — those surfaces intentionally stay solid | unchanged |

Software-rendered SwiftShader blurs are subtle; the refraction/lens is most
visible where the drawer and toolbar overlap other content. On real GPU hardware
the same shaders produce stronger distortion.

---

## 3. Feature smoke checks

The screenshots use real ViewModels and real stored data: the chat image shows
a stored user/assistant exchange created through `ChatViewModel`; the onboarding
image shows live permission state from the device; the provider card shows the
live built-in provider. Navigation, permission actions, composer, provider
configuration and settings are unchanged by this UI-only work. No claim is made
that every deep interaction was exercised through the emulator; the automated
unit tests and lint cover the shipped logic.

## 4. Automated cloud checks

- `HMXHARNESS Android CI` — unit tests, Android lint, debug + release APK.
- `HMXHARNESS Visual QA` — debug APK build and six real screen captures per
  theme, with logcat artifacts.
- All builds and captures run in GitHub Actions; no Gradle invocation was made
  locally.

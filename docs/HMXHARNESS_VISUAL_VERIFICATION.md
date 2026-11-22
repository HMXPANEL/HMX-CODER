# HMXHARNESS — Visual Verification

Status: **capture not yet performed** — blocked by missing repository write access
(see section 6). This document records the capture plan, the checks that will be
applied and the current verification state. No screenshot is claimed as evidence
before it exists.

---

## 1. Screens to capture (6)

| # | Screen | How it is rendered | File produced |
| --- | --- | --- | --- |
| 1 | Home | The shipped chat dashboard (`ChatScreen` with a real `ChatViewModel`, no conversation) | `01-home.png` |
| 2 | Navigation drawer | The real navigation shell (`AppNav`), drawer opened by an edge swipe | `02-navigation-drawer.png` |
| 3 | Settings | The shipped `SettingsScreen(container, onBack)` | `03-settings.png` |
| 4 | Providers | The shipped `ProvidersScreen(container, onBack)` | `04-providers.png` |
| 5 | AI Chat | The shipped `ChatScreen` with a stored conversation (user + assistant rows) | `05-ai-chat.png` |
| 6 | Onboarding | The installed APK's real entry point (`MainActivity`) on first run, plus `SetupScreen` | `06-onboarding.png` |

Rendering is done by a debug-only host (`app/src/debug/.../GlassShowcaseActivity.kt`)
that renders the app's own screen composables with the real `AppContainer`,
repositories and ViewModels. It is a screen selector, not a mock: no screen
layout, data or callback is duplicated, and the activity is absent from release
builds. Screenshots are taken with `adb exec-out screencap` on an API 33
`pixel_5` x86_64 emulator in dark mode with animations disabled.

Emulator script: `.github/workflows/android-visual-qa.yml`
Artifact: `hmxharness-liquidglass-screenshots`, retention 90 days.

## 2. Checks applied to each screenshot

1. Is the background genuinely visible through glass surfaces (not an opaque
   fill)?
2. Are backdrop effects visible on panels, rows, cards and the composer?
3. Do glass buttons replace the plain Material buttons on the required screens?
4. Do glass cards replace plain cards (provider rows, settings rows, starter
   cards, onboarding steps)?
5. Are old opaque settings rows still visible anywhere?
6. Is the navigation drawer actually translucent, with the content behind it
   visible?
7. Are reflections, specular highlights and edge light visible?
8. Is all text readable (no transparent text, no low-contrast text on glass)?
9. Does any screen still use the previous flat visual design?
10. Are press/selected states still legible?

## 3. Verification matrix (to be filled from the captured images)

| Check | Home | Drawer | Settings | Providers | AI Chat | Onboarding |
| --- | --- | --- | --- | --- | --- | --- |
| Backdrop visible through glass | pending | pending | pending | pending | pending | pending |
| Backdrop blur/refraction | pending | pending | pending | pending | pending | pending |
| Glass buttons | pending | pending | pending | pending | pending | pending |
| Glass cards/rows | pending | pending | pending | pending | pending | pending |
| No leftover flat containers | pending | pending | pending | pending | pending | pending |
| Drawer translucency | n/a | pending | n/a | n/a | n/a | n/a |
| Specular / edge light | pending | pending | pending | pending | pending | pending |
| Text contrast | pending | pending | pending | pending | pending | pending |

## 4. Automated checks already in the repository

- `:app:testDebugUnitTest` — unit tests including the glass mode resolution, the
  fresh-install defaults and the settings-search/agent-catalog discoverability.
- `:app:lintDebug` — Android lint against the current baseline.
- `:app:assembleDebug` / `:app:assembleRelease` — cloud-only build.
- Emulator logcat scan for `FATAL`, `AndroidRuntime`, `Exception`, `prismal` and
  `hmx.harness` errors after the capture.

## 5. Functional smoke checks planned after the build

- App startup into onboarding.
- Onboarding permission actions still trigger the real system dialogs.
- Drawer opens/closes and its destinations still navigate.
- Chat opens, the composer accepts text, the model selector opens.
- Provider screen lists providers and opens the API configuration sheet.
- Settings navigation opens the expected page and the search field works.
- Dialog actions (cancel/confirm) still behave.

## 6. Blocker

The previous token for `HMXPANEL/HMXHARNESS` was deleted. The only recoverable
credential belongs to `tanishka1233` (`pull: true`, `push: false` on both
`HMXPANEL/HMXHARNESS` and `HMXPANEL/HMX-CODER`). A read-only account cannot push
the branch, dispatch the visual QA workflow, or upload artifacts. Until a token
with `repo` and `workflow` scope is supplied, sections 1, 3 and 5 cannot be
completed and no screenshot or APK claim is made.

## 7. Previous evidence (pre-replacement)

Before this replacement, the visual QA run on `3df84a94` captured onboarding and
home frames that showed the glass engines disabled by default. That is the
baseline this replacement changes: `HARNESS_GLASS` and both settings defaults are
now `true`, so the same surfaces are expected to render glass instead of flat
containers. Those earlier images are superseded and will not be reused as
evidence for the new design.

# HMXHARNESS Liquid Glass Visual Report

## Evidence source

Actual screenshots captured by a cloud Android emulator (API 33, Pixel 5, dark
mode) from the installed debug APK — not mockups.

- Workflow: `.github/workflows/android-visual-qa.yml`
- Successful run: https://github.com/HMXPANEL/HMXHARNESS/actions/runs/44991926523
- Screenshot artifact: `hmxharness-liquidglass-screenshots` (3.2 MB)
- Included captures: `01-onboarding-dialog.png`, `02-onboarding.png`,
  `03-drawer.png`, `04-home-settled.png`.

## Honest screenshot assessment

**Valid evidence:** `02-onboarding.png` (dark mode, permission dialog dismissed)
shows the real app UI after this iteration:

- Deep near-black/violet background.
- Permission cards now have a thin luminous glass rim and layered fill rather
  than a purely flat outline.
- Primary/secondary buttons show real Prismal specular edge lighting and a
  violet-tinted glass body; the disabled "Start harness" button is visibly
  dimmed.
- Text remains fully opaque and readable; icon contrast is intact.
- The HMXHARNESS wordmark and progress indicator are present and unchanged.

**Limitations of the evidence (not glossed over):**

- The backdrop behind onboarding is a uniform dark surface, so backdrop blur
  and edge refraction are subtle in this particular capture; they are present
  in the implementation (PrismalAGSL AGSL lens + blur) but cannot be judged
  strongly from this screen alone.
- `03-drawer.png` and `04-home-settled.png` show the Android launcher — the
  scripted swipe/back exited the app before those captures. They are **not**
  valid evidence of drawer/home UI and are reported as such.
- Chat, settings, providers and dialogs were not captured in this run; they are
  pending visual confirmation on a device.
- I did not assign a numerical 90/100 score: the user requested lightweight
  verification, and the captured evidence is not sufficient for a credible
  per-screen score. No fabricated scoring is claimed.

## Changes made after visual inspection

The first valid screenshots showed flat onboarding cards and flat buttons.
I then applied `Modifier.liquidGlass` (PrismalAGSL) to:

- `ui/setup/SetupScreen.kt` permission cards.
- shared `Button`, `FilledTonalButton`, `OutlinedButton` in
  `ui/common/ExpressiveButtons.kt` (app-wide glass buttons).

The re-capture above shows the improved rims/highlights.

## Functional regression

- `:app:testDebugUnitTest` — PASSED in cloud CI.
- `:app:lintDebug` — PASSED in cloud CI.
- `:app:assembleDebug` / `:app:assembleRelease` — built in cloud CI with the
  pinned NDK, so native libraries are packaged.
- JNI symbols were corrected from `Java_com_hmx_harness_app_*` to
  `Java_com_hmx_harness_*`, fixing native method resolution after the package
  rename (found via emulator logcat).
- Interactive on-device smoke tests remain pending.

## Remaining work / recommendation

Inspect chat, providers, settings and navigation drawer on a real OPPO A38.
The design system is centralized, so further tuning is a small presentation
change rather than a rewrite.

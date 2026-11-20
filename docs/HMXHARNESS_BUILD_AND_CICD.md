# HMXHARNESS Build & CI/CD

## 1. Toolchain (inherited from upstream, not arbitrarily upgraded)

| Component | Version |
|---|---|
| Gradle wrapper | 9.7.0 (`gradle/wrapper/gradle-wrapper.properties`) |
| Android Gradle Plugin | 9.3.1 |
| Kotlin | 2.4.10 |
| KSP | 2.3.11 |
| JDK | 21 on CI runners (source/target compatibility Java 17) |
| Android SDK | compileSdk 37, targetSdk 36, minSdk 26 |
| Native (optional) | NDK 27.2.12479018, CMake 3.22.1, `glslc` |

`gradle.properties` sets a 4 GB Gradle heap and 8 GB Kotlin daemon heap (`org.gradle.jvmargs`, `kotlin.daemon.jvmargs`) because the app module is ~146k lines. This is why builds run in the cloud.

## 2. Build variants and outputs

| Variant | Application ID | Output |
|---|---|---|
| debug | `com.hmx.harness.debug` | `app/build/outputs/apk/debug/app-debug.apk` |
| release | `com.hmx.harness` | `app/build/outputs/apk/release/app-release.apk` |

Release uses `isMinifyEnabled = true`, `isShrinkResources = true`, R8 with `proguard-rules.pro`, and AGP's debug signing config (installable; not a production signature). `checkReleaseBuilds = false` is inherited because upstream's `lintVitalAnalyzeRelease` hits a lint toolchain bug on `HarnessUserService.kt`. The `benchmark` build type and `play` distribution are opt-in via Gradle properties and are not used by default.

## 3. Cloud-only workflows

- `.github/workflows/android-ci.yml` — on push to `main`/`feature/**`, PRs, and manual dispatch:
  wrapper validation → JDK 21 → Gradle cache → unit tests → `lintDebug` → `assembleDebug` + `assembleRelease` → APK existence/size checks → artifact `hmxharness-apks` → build summary.
- `.github/workflows/android-release.yml` — on `v*` tags and manual dispatch:
  wrapper validation → JDK 21 → Gradle cache → `assembleRelease` → verification → artifact `hmxharness-release-apk` → summary.

The workflows intentionally **do not install the pinned NDK or `glslc`**. `app/build.gradle.kts` detects the missing pinned NDK and configures the app without native code (`libharness_local.so`, GPU and SPAKE2 blocks omitted). This is a supported upstream fallback and yields a complete Kotlin/Compose APK. A full native build is available through the upstream GPU-runtime workflow pattern when the owner provisions the NDK and a GPU runtime release.

## 4. Local device
No local build steps are defined or required. Verification of downloaded artifacts uses lightweight tools (`aapt2`, `keytool`, `unzip`) on the control device — no Gradle and no Android Studio.

## 5. Signing
- Release: AGP debug signing config (same as upstream alpha distribution). No keystore or secret is committed.
- For production signing, add `signing-keys/release.jks` (gitignored) and CI secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`, decode the keystore in a temp file, sign, then delete it. Do not fake a signature.

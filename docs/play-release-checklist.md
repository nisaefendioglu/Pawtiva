# Google Play release checklist — Pawtiva 1.0.0

Requirements checked on **2026-11-05** against official Google pages (sources at the end). Re-check before submitting; Play policies change.

> **Publishing is the developer's own action.** Nothing in this project uploads, publishes or submits anything. Every Play Console step below is done by the developer, who decides when to release.

## 1. Technical requirements

| Requirement | Status in Pawtiva | Source |
| --- | --- | --- |
| Target API level: new apps and updates must target **Android 16 (API 36)** or higher from 31 Aug 2026 (extension possible to 1 Nov 2026) | `targetSdk = 36` ✅ | [Target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en-419) |
| Android App Bundle (.aab) upload with **Play App Signing** (required when publishing a new app) | Build with `./gradlew :app:bundleRelease`; enroll in Play App Signing on first upload | [Sign your app](https://developer.android.com/studio/publish/app-signing?hl=en) |
| Signing key validity must end after 22 Oct 2033; 25+ years recommended | Use `-validity 10000` (≈27 years) below | [Sign your app](https://developer.android.com/studio/publish/app-signing?hl=en) |
| 16 KB memory page size support for apps targeting API 35+; updates that don't support it are blocked from **1 Feb 2027** | Pure Kotlin/Java app, so supported by default. Confirm with APK Analyzer that the bundle has no `lib/*.so` files. | [Support 16 KB page sizes](https://developer.android.com/16kb-page-size) |
| Release build minified (R8) and shrunk | `isMinifyEnabled = true`, `isShrinkResources = true` ✅ | — |
| versionCode / versionName | 1 / 1.0.0 (increase versionCode for every upload) | — |

## 2. Account requirement for new personal developer accounts

Personal accounts created after 13 Nov 2023 must run a **closed test with at least 12 testers opted in for 14 consecutive days** before applying for production access. A tester who leaves and rejoins restarts their count. ([App testing requirements](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en))

Plan: upload the AAB to a closed testing track, invite ≥12 testers (Google Group or email list), keep them opted in for 14 days, collect feedback, then apply for production access.

## 3. Store listing assets

| Asset | Spec | Pawtiva file |
| --- | --- | --- |
| App icon | 512 × 512, 32-bit PNG with alpha, ≤ 1024 KB | `store/graphics/icon-512.png` |
| Feature graphic | 1024 × 500, JPEG or 24-bit PNG (no alpha) | `store/graphics/feature-graphic-1024x500.png` |
| Phone screenshots | At least 2 (up to 8 per device type); JPEG or 24-bit PNG; each side 320–3840 px, long side ≤ 2× short side; 9:16 at ≥ 1080 × 1920 recommended | **To capture from the real app** (see `store/graphics/README.md`) |
| Alt text | ≤ 140 characters per graphic | Suggested below |
| Text rules | No "Best", "#1", "Free", "Sale", rankings, device frames, third-party logos or store badges | Graphics follow these rules |

Source: [Preview assets](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en-GB)

Suggested alt text:
- Icon: "Purple paw print on a soft lavender background"
- Feature graphic: "A kitten and a puppy next to the Pawtiva name and the line: Build habits, grow together"

Listing texts: `store/listing-en.md`, `store/listing-tr.md`.

## 4. App content (Play Console → Policy → App content)

- [ ] **Privacy policy** URL — required for all apps, even with no data collection. Host `docs/privacy-policy-en.md` publicly. ([Data safety](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en))
- [ ] **Data safety** — answers in `docs/play-data-safety.md` (no data collected or shared).
- [ ] **Ads** — "No".
- [ ] **Content rating** — notes in `docs/content-rating.md`.
- [ ] **Target audience** — open decision (13+ recommended unless designing for children).
- [ ] **App access** — all functionality available without login.
- [ ] Category: Productivity (or Lifestyle); contact email; optional website.

## 5. Create the upload key (once, on your Mac)

Do this yourself; never commit the keystore or passwords.

```bash
# 1. Create the upload keystore outside the project folder
keytool -genkeypair -v \
  -keystore ~/keys/pawtiva-upload.jks \
  -alias pawtiva-upload \
  -keyalg RSA -keysize 4096 -validity 10000

# 2. Create keystore.properties in the project root (already in .gitignore)
cat > keystore.properties <<'EOF'
storeFile=/Users/<you>/keys/pawtiva-upload.jks
storePassword=<store password>
keyAlias=pawtiva-upload
keyPassword=<key password>
EOF
```

Back up the `.jks` file and passwords in a password manager. If the upload key is lost, it can be reset through Play Console support; the app signing key itself is held by Google with Play App Signing.

The release signing configuration in `app/build.gradle.kts` must read `keystore.properties` only if it exists, so debug builds keep working without it. Reference snippet:

```kotlin
import java.util.Properties

val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

android {
    signingConfigs {
        if (keystoreProperties.isNotEmpty()) {
            create("release") {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.findByName("release")
        }
    }
}
```

## 6. Build the release bundle

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew clean testDebugUnitTest lintRelease :app:bundleRelease
# Output: app/build/outputs/bundle/release/app-release.aab
```

Before uploading:
- [ ] Install a release build on a real device (`bundletool` or an internal test track) and walk through onboarding, habits, shop, statistics, settings, reminders, export and reset in both Turkish and English, light and dark theme.
- [ ] Check the merged release manifest for unexpected permissions.
- [ ] Open the AAB in APK Analyzer: no `lib/` native libraries (16 KB rule).
- [ ] Capture phone screenshots.

## 7. Upload and release (developer only)

1. Play Console → Create app → name, default language, app/free, declarations.
2. Complete App content (section 4) and the store listing (section 3).
3. Testing → Closed testing → create a track, upload `app-release.aab`, accept Play App Signing, add testers.
4. After 14 days with ≥ 12 testers: apply for production access, then create a production release.
5. Review the pre-launch report and Android vitals.

---

Sources (checked 2026-11-05):
- [Target API level requirements for Google Play apps](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en-419)
- [App testing requirements for new personal developer accounts](https://support.google.com/googleplay/android-developer/answer/14151465?hl=en)
- [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en-GB)
- [Provide information for Google Play's Data safety section](https://support.google.com/googleplay/android-developer/answer/10787469?hl=en)
- [Sign your app](https://developer.android.com/studio/publish/app-signing?hl=en)
- [Support 16 KB page sizes](https://developer.android.com/16kb-page-size)

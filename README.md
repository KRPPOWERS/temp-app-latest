# Arulmigu Kallichi Amman Temple — Android App
## Build & Install Instructions

---

## OPTION A — Build with Android Studio (Recommended, Free)

### Step 1: Install Android Studio
1. Download from: https://developer.android.com/studio
2. Install it (Windows/Mac/Linux)
3. During setup, let it install the Android SDK automatically

### Step 2: Open the Project
1. Open Android Studio
2. Click **"Open"** (not "New Project")
3. Navigate to and select the **TempleApp** folder
4. Wait for Gradle sync to complete (2-5 minutes first time, needs internet)

### Step 3: Build the APK
1. Click menu: **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build to finish
3. Click **"locate"** in the popup, OR find the APK at:
   `TempleApp/app/build/outputs/apk/debug/app-debug.apk`

### Step 4: Install on Your Android Phone
**Method 1 — USB Cable:**
1. On your phone: Settings → Developer Options → Enable USB Debugging
   (To enable Developer Options: Settings → About Phone → tap "Build Number" 7 times)
2. Connect phone to PC via USB
3. In Android Studio: Click the ▶ Play button (it will install directly)

**Method 2 — Copy APK to Phone:**
1. Copy `app-debug.apk` to your phone (via USB, WhatsApp, Google Drive, etc.)
2. On phone: Open the APK file
3. If prompted "Install from unknown sources" → Allow → Install

---

## OPTION B — Build Online (No PC Setup Needed)

### Using GitHub Actions (Free):
1. Create a free account at https://github.com
2. Create a new repository, upload the entire TempleApp folder
3. Create file `.github/workflows/build.yml` with content:
```yaml
name: Build APK
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - run: chmod +x gradlew
      - run: ./gradlew assembleDebug
      - uses: actions/upload-artifact@v3
        with:
          name: temple-app
          path: app/build/outputs/apk/debug/app-debug.apk
```
4. Push to GitHub — it builds automatically
5. Download APK from Actions tab → Artifacts

---

## FEATURES IN THE APP
- ✅ Full temple management system (all 9 modules)
- ✅ Admin login (password: Amman@1729) / View-only mode
- ✅ Automatic Google Sheets sync (push on save, pull every 30s)
- ✅ Works offline — data saved in phone storage
- ✅ Donation receipts with print support
- ✅ Excel & PDF export
- ✅ Dark temple-themed UI
- ✅ Back button navigates within the app
- ✅ Syncs with PC browser version automatically

## TECHNICAL DETAILS
- Min Android Version: Android 5.0 (API 21) and above
- Internet Permission: Required for Google Sheets sync
- Storage: Data stored in WebView localStorage (phone memory)
- App Size: ~500KB

## UPDATING THE APP
When you update the HTML file:
1. Replace `app/src/main/assets/index.html` with the new file
2. Rebuild the APK (Step 3 above)
3. Reinstall on phone

---
*Built with Android WebView — wraps the temple management HTML app natively*

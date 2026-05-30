# Aurora Music - Liquid Glass Music Space 🛰️

Aurora Music is a premium, minimal, state-of-the-art native Android music application styled around a gorgeous translucent **Liquid Glass** aesthetic. Driven by high-performance sound technology and seamless streaming connectors, Aurora provides an industrial-grade Hi-Fi listening room right on your mobile screen.

---

## 🎨 Visual Identity & Style

- **Realistic Liquid Glass Effect**: Translucent, double-bordered frosted cards using real-time radial gradients mirroring current album artwork hues.
- **Micro-animations (60fps+)**: Fluid, spring-based sliding full-deck overlays and spinning disc visuals.
- **Monochrome Dark Palette**: Fully optimized dark Slate interface paired with hyper-crisp Aqua accents for a tactical, luxury look.

---

## 🚀 Key Modules and Integrations

### 1. Unified Background Playback Engine (`AudioPlayerManager`)
- **Seamless Crossfade**: Dynamic overlapping crossfades utilizing dual `MediaPlayer` engines for gapless acoustic flow.
- **Smart Silence Detection**: Automated silence skip triggering past an adjustable silence gateway limit (Default: 12 seconds).
- **Physical Controls**: Interactive Pitch scaling (0.5x to 1.5x) and Playback Speed Velocity controls (0.5x to 2.0x).
- **Hardware Integration**: Real system frequency Equalizer (binding Rock, Metal, Class, Dance presets to the active session id) and custom Sleep Timers.
- **Real-time Synced Lyrics**: Scans, decodes, and scrolls song sync lyric timestamps dynamically with 100% position accuracy.

### 2. Multi-Store Sync & Search Cabin
- **Spotify Web API Connectivity**: Simple OAuth token configuration screen allowing real syncing of Liked Songs, private Playlists, profile pictures, and metadata.
- **Google YouTube Search**: Custom Retrofit interface to query live recordings, instrumentals, or LoFi cover tracks and import them directly as dynamic tracks in Room database.

### 3. Smart Local Offline Manager (`MusicRepository`)
- **Folder and SD Card Scanning**: Fully integrated metadata catalog indexing system mapping physical `/Music` and SD storage folders on-demand.
- **Room Persistence Engine**: Local standard SQLite mapping of Tracks, custom smartest Playlists criteria configurations, and dynamic playback logs.
- **ID3 Metadata Header Tag Editor**: Custom in-app metadata editor dialogue allowing instant correction of Title, Artist, and Album parameters inside the Room record.
- **Storage and Download Space Monitor**: Professional segmented visual map mapping used local cache storage space against total available physical storage.

---

## 🏗️ Premium MVVM Architectural Design

The application complies strictly with standard Clean Architecture principles:
- **Data Layer (`com.example.data`)**: Exposes Retrofit network resources & Room SQLite persistence repositories safely abstracted through standard local/remote repository managers.
- **Dependency Injection (`com.example.di`)**: Hand-crafted self-instantiating `AppContainer` singletons binding safe, thread-secure database and network instances during start. No boilerplates or heavy DI execution overheads.
- **Presentation Layer (`com.example.ui.viewmodel`)**: Employs lifecycle-aware ViewModels exposing immutable `StateFlow` structures consumed safely by Jetpack Compose.

---

## 🛠️ Build and Compilation Instructions

### Prerequisite System Requirements
- **JDK Version**: Java Development Kit 11 (or higher)
- **Android Studio SDK Toolkit**: Target API level 36, minimum support SDK 24.
- **Command Line**: Standard Gradle build CLI.

### Compilation CLI Commands

Ensure you pass standard `gradle` commands without the prepending `./gradlew` execution wrapper, which is unsupported in the target execution space:

#### 1. Compile the Application Structure:
```bash
gradle assembleDebug
```

#### 2. Run All Automated Unit & Robolectric Tests:
```bash
gradle :app:testDebugUnitTest
```

#### 3. Update Visual Layout Screenshot Reference Maps (Roborazzi):
```bash
gradle :app:recordRoborazziDebug
```

#### 4. Run Screenshot Layout Regression Verification:
```bash
gradle :app:verifyRoborazziDebug
```

### Build APK File Package Guide

To compile a debug or release Android APK package ready for installation:

#### Assemble Debug APK:
Executing this command compiles the Kotlin sources and packages the materials into an installer file located at `/app/build/outputs/apk/debug/app-debug.apk`:
```bash
gradle :app:assembleDebug
```

#### Assemble Release APK:
To build an ultra-minified, release-ready binary (configured with the local upload key in Gradle configs):
```bash
gradle :app:assembleRelease
```

---

## 🔐 Security Key Configurations

The template makes use of the Android Secrets Gradle Plugin which abstracts token injections.
To activate YouTube queries and Spotify integrations, edit the secured secrets inside AI Studio panel or provide standard fields inside the `.env` root manifest:
```env
GEMINI_API_KEY=your_key
YOUTUBE_API_KEY=your_google_cloud_youtube_credentials
```
Alternatively, users can copy/paste dynamic OAuth tokens on-the-fly inside the Discover linkages interface without any reboot required!

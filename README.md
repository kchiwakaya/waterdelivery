## H2O – Fleet & Water Delivery (Android)

A mobile app for managing water delivery operations. Dispatchers, drivers, and fleet owners can register trucks, select a truck to operate, and sync essential data in real time. Built with Jetpack Compose and Firebase to provide a modern, responsive UI and reliable cloud-backed storage.

### Why it matters

- **Operational clarity**: Assign and switch trucks quickly to reduce dispatch friction.
- **Real‑time data**: Cloud sync via Firebase keeps truck info up to date across devices.
- **Modern Android UX**: Compose + Material 3 delivers a fast and accessible experience.

---

## 🛠️ Tech Stack

- **Language**: Kotlin (JVM target 11)
- **UI**: Jetpack Compose, Material 3, Material Icons
- **Navigation**: Navigation Compose
- **Firebase**: Firestore KTX, Firebase Auth KTX, Google Play Services Auth; configured via `google-services.json`
- **Android**: minSdk 27, targetSdk 34, compileSdk 35
- **Build**: Gradle (Kotlin DSL), Compose BOM, Firebase BOM, ProGuard (disabled for release by default)
- **AndroidX**: `core-ktx`, `lifecycle-runtime-ktx`, `activity-compose`
- **Testing**: JUnit4, AndroidX Test (JUnit, Espresso), Compose UI Test

---

### Module

- **App ID / Namespace**: `com.balckliquid.h20`
- **Entry Activity**: `TruckSelectionActivity` (Compose UI backed by Firebase Firestore)

### Notes

- Ensure Firebase project is set up and `app/google-services.json` is present for builds.

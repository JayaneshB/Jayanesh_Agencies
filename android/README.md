# Choco Wholesale - Customer Android App

Jetpack Compose Android app for wholesale chocolate/sweets ordering.

## Tech Stack
- **Kotlin + Jetpack Compose** — Declarative UI
- **MVVM + Clean Architecture** — Testable layers
- **Hilt** — Dependency injection
- **Retrofit + OkHttp + Moshi** — Networking
- **Room** — Local cart/product caching
- **Coil** — Image loading
- **DataStore** — JWT token storage

## Build & Run

```bash
# Ensure Android SDK is installed and ANDROID_HOME is set
cd android
./gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

## Backend Connection
- **Emulator:** Points to `http://10.0.2.2:8080/api/` (configured in `app/build.gradle.kts`)
- **Physical device:** Change `BASE_URL` in `app/build.gradle.kts` to your machine's LAN IP
- Run the Spring Boot backend first: see `../backend/` README

## Screens
| Screen | Description |
|--------|-------------|
| Phone Entry | Enter mobile number, request OTP |
| OTP Verify | Enter 6-digit OTP code |
| Profile Setup | Name, business name, GSTIN (first-time only) |
| Home | Category chips + product grid |
| Search | Debounced server-side product search |
| Product Detail | Image, pricing tiers, stock, quantity stepper, add to cart |
| Cart | Line items, address selection, place order |
| Orders | Order history list with status chips |
| Order Detail | Items, amounts, status timeline |
| Profile | User info, saved addresses CRUD, logout |

## Dev OTP
OTPs are logged to the backend console during development. Check the Spring Boot logs for the OTP code after requesting it.

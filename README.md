# PoliceSimulator3D

A native Android police-patrol simulator prototype inspired by the open-world patrol loop of LSPDFR. This project is built **without a game engine**: Kotlin, the Android SDK, and a custom OpenGL ES 2.0 renderer.

## Current direction

- Native Android app written in Kotlin.
- Custom 3D rendering through Android's `GLSurfaceView` / `GLSurfaceView.Renderer` APIs.
- Procedurally generated low-poly city blocks; no Unity, Unreal, Godot, or external 3D asset pack.
- Third-person police cruiser driving and touch controls designed for landscape phones.
- Dispatch callouts, a simple progression/score loop, traffic vehicles, emergency lightbar, and synthesized siren audio.
- Offline-first; no account, network, or analytics requirement.

## Build

Open the project folder in a recent Android Studio installation with JDK 17, Android SDK Platform 36, and Gradle 8.13 available. Or run the included CI build workflow.

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

The Android Gradle Plugin is pinned to 8.13.2 and Kotlin to 2.2.21 to keep builds reproducible. The app targets Android API 35 and supports API 26+.

## Controls

- **STEER ◀ / ▶** — steer the patrol car.
- **ACCEL** — accelerate while held.
- **BRAKE** — brake while held.
- **SIREN** — toggle the lightbar and synthesized siren.
- **INTERACT** — resolve a callout when close enough (some callouts require the vehicle to be stopped).
- **NEW CALL** — request another dispatch assignment.

## Scope and roadmap

This is an early playable foundation, not a claim of feature parity with a full commercial open-world game. Planned systems include richer traffic behavior, on-foot officer controls, vehicle stops, citation/arrest interactions, pursuits, better 3D assets, settings, and saved progress.

## Technical references

- [Android OpenGL ES overview](https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl)
- [Displaying graphics with OpenGL ES](https://developer.android.com/develop/ui/views/graphics/opengl)
- [Android Gradle Plugin release notes](https://developer.android.com/build/releases/gradle-plugin)

## License

No third-party game assets are bundled. A repository license should be chosen by the maintainer before outside contributions or asset packs are accepted.

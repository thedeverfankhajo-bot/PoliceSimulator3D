# PoliceSimulator3D

Native Android police-patrol simulator inspired by the open-world patrol loop of LSPDFR. The project is built **without a game engine** using Kotlin, the Android SDK, and a custom OpenGL ES 2.0 renderer.

## Current Android implementation

- Native Android app entry point: `app/src/main/java/com/erfan/policesimulator/MainActivity.kt`
- Custom 3D scene and gameplay loop: `GameEngine.kt`
- OpenGL ES 2.0 via Android `GLSurfaceView` and `GLSurfaceView.Renderer`
- Procedurally generated low-poly city blocks, with CC0 Poly Haven Asphalt 03 road texturing and Concrete Floor facade texturing
- Third-person patrol driving with touch controls for steering, acceleration and braking
- Building collision for patrol-vehicle and on-foot movement, including wall sliding, world bounds and sub-stepped movement to reduce tunnelling
- Animated red/blue emergency lightbar, native synthesized siren/engine/impact audio, and a basic walking gait for the officer
- Dispatch callouts, suspect stop, on-foot field contact and licence/warrant records check
- Explicit **ARREST** versus **CITE / WARN** decision with separate outcomes and score penalties for an unsupported decision
- OpenGL rendering pauses and resumes with the Android activity lifecycle
- Career XP, completed calls and decision accuracy persist locally with AndroidX DataStore
- Landscape full-screen layout; offline-first, no analytics or account requirement

This is an early playable foundation, not feature parity with LSPDFR or a commercial open-world game. The world uses procedural low-poly geometry together with licensed CC0 diffuse textures. Siren, engine and impact sounds are synthesized locally so the app does not need bundled copyrighted audio. Vehicle-to-vehicle collision physics, richer pedestrian AI, advanced pursuit behavior, recorded radio voice, and map navigation remain future work. Career persistence is local to the device and is not synced to a server.

## Build the Android app

Requirements: JDK 17, Android SDK Platform 36, and Gradle 8.13.

```bash
gradle --no-daemon :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The app compiles and targets Android 16 (API 36). The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions runs local unit tests, Android Lint and the debug APK build on pushes and pull requests targeting `main`, then uploads the APK as a workflow artifact. Incident decisions, career progression, and the world collision solver are pure Kotlin rules covered by unit tests; device-level graphics and gameplay still need manual testing on Android hardware.

## Controls

- **◀ / ▶** — steer while held
- **ACCEL / BRAKE** — drive while held
- **LIGHTS / SIREN** — toggle emergency lights and siren state
- **EXIT / ENTER** — leave or enter the patrol car (stop first)
- **INTERACT** — progress the field contact when in range
- **NEW CALL** — accept a call when available or after a call is complete

## Technical references

- [Android OpenGL ES overview](https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl)
- [Android AudioTrack](https://developer.android.com/reference/android/media/AudioTrack)
- [Poly Haven CC0 license](https://polyhaven.com/license)
- [Build an OpenGL ES environment](https://developer.android.com/develop/ui/views/graphics/opengl/environment)
- [Displaying graphics with OpenGL ES](https://developer.android.com/develop/ui/views/graphics/opengl)
- [GLSurfaceView lifecycle requirements](https://developer.android.com/reference/android/opengl/GLSurfaceView)
- [Android local unit tests](https://developer.android.com/studio/test/test-in-android-studio)
- [AndroidX DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- [Android Gradle Plugin release notes](https://developer.android.com/build/releases/gradle-plugin)
- [GitHub Actions: build and test Java with Gradle](https://docs.github.com/actions/tutorials/build-and-test-code/java-with-gradle)

## License

Texture sources and licensing are recorded in [`ASSET_CREDITS.md`](ASSET_CREDITS.md). All included third-party texture files are CC0 1.0; synthesized sound contains no third-party sample. A repository software license should still be chosen by the maintainer before outside contributions.

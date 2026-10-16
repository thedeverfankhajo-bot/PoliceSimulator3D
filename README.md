# PoliceSimulator3D

Native Android police-patrol simulator inspired by the open-world patrol loop of LSPDFR. The project is built **without a game engine** using Kotlin, the Android SDK, and a custom OpenGL ES 2.0 renderer.

## Current Android implementation

- Native Android app entry point: `app/src/main/java/com/erfan/policesimulator/MainActivity.kt`
- Custom 3D scene and gameplay loop: `GameEngine.kt`
- OpenGL ES 2.0 via Android `GLSurfaceView` and `GLSurfaceView.Renderer`
- Procedurally generated low-poly city blocks, roads, windows, streetlights, traffic and patrol/suspect vehicles
- Third-person patrol driving with touch controls for steering, acceleration and braking
- Emergency lightbar toggle, dispatch callouts, suspect stop, on-foot field contact, warrant check and score progression
- Landscape full-screen layout; offline-first, no analytics or account requirement

This is an early playable foundation, not feature parity with LSPDFR or a commercial open-world game. The current world uses procedural low-poly geometry rather than external game assets. Vehicle collision physics, realistic pedestrian AI, pursuit behavior, persistent save data, audio, map navigation and a full arrest/citation decision UI remain future work.

## Build the Android app

Requirements: JDK 17, Android SDK Platform 35, and Gradle 8.13.

```bash
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`. GitHub Actions builds and tests the app on pushes and pull requests targeting `main`, and uploads the debug APK as a workflow artifact.

## Controls

- **◀ / ▶** — steer while held
- **ACCEL / BRAKE** — drive while held
- **LIGHTS / SIREN** — toggle emergency lights and siren state
- **EXIT / ENTER** — leave or enter the patrol car (stop first)
- **INTERACT** — progress the field contact when in range
- **NEW CALL** — accept a call when available or after a call is complete

## Technical references

- [Android OpenGL ES overview](https://developer.android.com/develop/ui/views/graphics/opengl/about-opengl)
- [Build an OpenGL ES environment](https://developer.android.com/develop/ui/views/graphics/opengl/environment)
- [Displaying graphics with OpenGL ES](https://developer.android.com/develop/ui/views/graphics/opengl)
- [Android Gradle Plugin release notes](https://developer.android.com/build/releases/gradle-plugin)
- [GitHub Actions: build and test Java with Gradle](https://docs.github.com/actions/tutorials/build-and-test-code/java-with-gradle)

## License

No third-party game assets are bundled. A repository license should be chosen by the maintainer before outside contributions or asset packs are accepted.

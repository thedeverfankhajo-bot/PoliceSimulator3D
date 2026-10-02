# Development Status

## Verification rule

No feature is complete until it has been implemented, reviewed against the relevant official documentation, and verified by an appropriate automated test, CI check, or reproducible local check.

## Repository baseline

- Default branch is `main`.
- Legacy bootstrap branches were identified during repository audit; no new feature branches should be created for routine work.
- CI validates the default branch with least-privilege permissions and a pinned GitHub Action.
- Godot 4.7.2 Linux x86_64 is pinned in CI by SHA-256:
  `cadd3204e728a35d3f13adb7fd0d7902636b79f6b95c40c265eb73b6c35329e4`.
- Secrets, signing credentials, and generated build artifacts are excluded from version control.

## Current gameplay foundation

- Godot main scene and bootstrap.
- First-person police player controller.
- Safe raycast interaction detector.
- Enter/exit police vehicle flow with exit-space validation.
- Police vehicle four-wheel composition and interaction contract.
- Civilian NPC interaction contract.
- Mission and mission-manager state flow.
- Traffic vehicle movement, violation detection, and police stop state.
- Traffic-stop vertical slice: enter patrol vehicle → stop traffic vehicle → interact with civilian → mission completion.
- Headless mission and traffic-vehicle tests.
- GDScript parser validation and main-scene smoke validation in CI.

## Known verification gaps

- Exact Godot editor version used on the developer device still needs local verification.
- Android export toolchain and signed release build still need verification on a supported environment.
- Touch/mobile controls need implementation and device testing.
- Real-device performance baseline still needs measurement before optimization claims.
- Repository branch-protection/ruleset enforcement must be verified in GitHub settings before being described as enabled.
- Advanced vehicle physics remain subject to Godot's documented VehicleBody3D/VehicleWheel3D limitations; realistic vehicle dynamics require additional validation or a custom physics approach.

## Continuous work

Bug fixing, missing-piece analysis, gameplay development, testing, security review, performance measurement, documentation, and release hardening are continuous activities rather than separate end phases.

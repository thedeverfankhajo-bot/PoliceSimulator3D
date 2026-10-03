# Development status

## Verified repository baseline

The project is an active early-development Godot 4.7 police-simulator vertical slice. It is not yet a finished game and the current procedural 3D content is intentionally lightweight while gameplay architecture is being expanded.

### Implemented

- first-person player movement and interaction;
- police vehicle entry/exit;
- mobile input including a dedicated brake action;
- traffic movement and waypoint support;
- speeding detection with structured violation evidence;
- traffic-stop mission lifecycle and timeout handling;
- mission/status/help HUD separation;
- procedural starter-city composition;
- procedural police-vehicle and civilian-NPC geometry;
- police-station interior dressing;
- settings persistence and reset support;
- save/continue infrastructure and auto-save setting;
- main-menu settings and tutorial panels;
- GitHub Actions repository validation;
- career regression checks;
- Android debug APK export and artifact inspection in CI.

### Important verification boundary

A successful headless Linux Godot test proves only the behavior covered by that test environment. It does not prove visual quality, every Android-device configuration, touch behavior on every screen size, GPU performance, or store-release readiness. Android real-device testing remains a separate gate.

## Repository quality and licensing work

The original project source code is now licensed under the MIT License in the repository root. This license does not automatically apply to third-party assets, addons, fonts, sounds, models, textures, or other dependencies.

Third-party material must have a known source and compatible usage terms before redistribution. Provenance and license information belongs in `docs/CITATIONS.md`.

The repository also uses GitHub Actions validation, Dependabot for GitHub Actions updates, security guidance, and focused Conventional Commits as part of the maintenance workflow.

## Known development gaps

These are active development areas rather than claims of completed functionality:

- replace more procedural placeholder geometry with licensed, documented production assets;
- expand the city into a larger navigable environment with more varied buildings, roads, interiors, pedestrians, and traffic;
- deepen police-station interactions and interior gameplay;
- expand civilian and police NPC behavior and animation;
- add additional evidence-backed traffic violations and mission types;
- expand settings into display, accessibility, control, audio, and graphics options that are actually wired to runtime behavior;
- improve in-game tutorial guidance and contextual help;
- expand save data coverage and add explicit save/load regression tests;
- perform Android real-device profiling and memory/GPU testing;
- continue UI, lighting, materials, audio, VFX, and performance work without claiming optimization until measured.

## Verification policy

Every behavior change should be followed by the smallest relevant Godot tests and repository validation. GitHub Actions results are reported from the actual workflow state; queued or running jobs are not reported as successful.

## Release policy

Only intentionally versioned source files belong in Git. Generated exports and build artifacts remain CI artifacts unless a release process explicitly requires them. Android debug APKs are validation artifacts, not release builds.

## Asset policy

Third-party assets must have a known source, license/usage terms, attribution requirements, and repository path recorded in `docs/CITATIONS.md` before they are treated as project assets.

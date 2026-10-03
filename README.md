# PoliceSimulator3D

PoliceSimulator3D is an open-source, modular 3D police simulation game built with Godot 4.7.

## Current status

This repository is in active early gameplay development and is not presented as a finished commercial-quality game. The current verified vertical slice includes:

- first-person player movement and interaction;
- police vehicle entry/exit;
- mobile input with a dedicated brake action;
- traffic vehicle movement and waypoint support;
- speeding detection and structured violation evidence;
- traffic-stop mission lifecycle and timeout handling;
- mission/status/help HUDs;
- procedural starter-city composition;
- procedural police-vehicle and civilian-NPC geometry;
- police-station interior dressing;
- settings persistence and save/continue infrastructure;
- tutorial and menu flows;
- headless gameplay and repository validation in GitHub Actions;
- Android debug APK export in GitHub Actions.

## Project goals

- Modular and maintainable gameplay architecture
- Safe Git/GitHub workflow
- Deterministic, testable core systems where practical
- Performance-aware 3D design
- Android and desktop support

## Repository structure

```text
assets/       Source assets and game resources
scenes/       Godot scenes
scripts/      Runtime and gameplay code
data/         Data-driven game definitions
tests/        Automated tests
docs/         Architecture and development documentation
.github/      CI and repository automation
```

## Development and verification

Changes are kept small and are verified through the repository's automated checks before being treated as complete. Godot engine/platform behavior is checked against official Godot documentation where relevant. GitHub Actions is used for repository validation, regression checks, and Android debug export.

A successful Linux/headless CI run does **not** prove that the game is fully compatible with every Android device. Real-device Android testing remains a separate release gate.

Do not commit generated/imported files unless they are intentionally source-controlled. Never commit secrets, signing credentials, API keys, keystores, or private keys.

## Asset and licensing policy

Original project source code is released under the MIT License. Third-party assets, addons, fonts, sounds, and other resources remain subject to their own licenses and are not automatically relicensed by this repository. Before adding a Marketplace or other third-party asset, record its source and license/usage terms in `docs/CITATIONS.md` and keep incompatible or unclear assets out of the repository.

## Security

See [SECURITY.md](SECURITY.md). Security issues should be reported privately rather than disclosed in a public issue.

## License

The original source code of this project is licensed under the [MIT License](LICENSE).

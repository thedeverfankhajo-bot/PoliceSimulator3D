# PoliceSimulator3D

A modular 3D police simulation game built with Godot.

## Project goals

- Modular and maintainable gameplay architecture
- Safe Git/GitHub workflow
- Deterministic, testable core systems where practical
- Performance-aware 3D design
- Android and desktop support as the project matures

## Development principles

1. Verify engine and platform behavior against official documentation before relying on it.
2. Keep generated/imported files out of Git unless they are intentionally source-controlled.
3. Never commit secrets, signing credentials, or private keys.
4. Prefer small, reviewable commits.
5. Keep gameplay systems separated from presentation and asset data.
6. Profile before making performance claims or optimizations.

## Initial architecture

```text
assets/       Source assets and game resources
scenes/       Godot scenes
scripts/      Runtime/gameplay code
data/         Data-driven game definitions
tests/        Automated tests
addons/       Third-party Godot addons, if any
examples/     Small isolated examples when useful
docs/         Architecture and development documentation
.github/      CI and repository automation
```

## Status

Active early gameplay development. The current vertical slice includes first-person movement, police vehicle entry/exit, mobile controls, traffic movement and speeding detection, structured violation evidence, and a traffic-stop mission.

## Security

See [SECURITY.md](SECURITY.md).

## License

License will be selected before redistributing the project.

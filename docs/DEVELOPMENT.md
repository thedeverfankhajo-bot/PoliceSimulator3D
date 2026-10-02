# Development Guide

## Source of truth

When choosing an engine feature, export setting, Android setting, GitHub security feature, or Termux command, verify the behavior against the current official documentation before implementation.

## Workflow

1. Create a focused branch from `main`.
2. Make one small, logically complete change.
3. Run the relevant checks locally.
4. Review the diff for accidental files and secrets.
5. Open a pull request when a change is ready for review.
6. Merge only after required checks pass.

## Project organization

- `scenes/` contains `.tscn` scene resources.
- `scripts/` contains reusable runtime logic.
- `data/` contains data-driven definitions rather than hard-coded values where practical.
- `assets/` contains source-controlled game assets that are actually part of the project.
- `.godot/` is generated/local project data and is ignored.

## Performance

Do not optimize based on assumptions. Establish a measurable baseline first, identify the actual bottleneck with profiling, make the smallest useful change, and measure again.

For mobile targets, test on representative physical devices rather than relying only on desktop performance.

## Security

- Secrets stay outside Git.
- Android signing material stays outside Git.
- Third-party dependencies are introduced deliberately and documented.
- Network-facing systems must validate untrusted input.
- Save data must be treated as untrusted input if it can be modified outside the game.

## Termux

Termux is a development environment, not the game runtime itself. Keep build scripts reproducible and avoid depending on undocumented device-specific paths or global state.

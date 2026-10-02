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


## Asset pipeline

For environment and road presentation, prefer assets from the official Godot Asset Store and document the exact asset, version, license, and intended platform before importing them. Godot 4.7 introduced the Asset Store as the successor to the older Asset Library; assets are installed from inside the editor or from the official store website.

Current candidates reviewed for this project:

- **Godot Road Generator** — MIT, tested with Godot 4.7, useful for procedural roads, lanes, intersections, and traffic paths. It is especially relevant because the traffic simulation already needs a more scalable road/lane foundation.
- **KayKit City Builder Bits** — CC0, optimized low-poly city assets with an atlas texture and mobile suitability. These are suitable for replacing the current placeholder environment after import/visual QA.
- **Prototype Texture Materials** — MIT, Godot 4.7, a candidate for placeholder material quality while the final environment art pipeline is built.

Do not copy paid/proprietary assets or undocumented downloads into Git. Record the source, version, license, and compatibility before committing imported assets.

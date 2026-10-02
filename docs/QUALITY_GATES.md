# Quality Gates

This project treats implementation and verification as separate states.

## Gate 1 — Repository
- Required files exist.
- No tracked secret/signing material.
- Text files use LF.
- CI workflow uses least-privilege permissions.
- Third-party GitHub Actions are pinned to full commit SHAs.

## Gate 2 — Godot static/runtime
- Project opens with the declared Godot major/minor version.
- Main scene loads.
- Scripts parse without errors.
- Input actions resolve.
- Physics interactions execute without errors.

## Gate 3 — Gameplay
- Player can move.
- Interaction ray reaches intended targets.
- Traffic-stop mission starts through MissionManager.
- Vehicle and civilian objectives complete exactly once.
- Mission reaches COMPLETED only after all objectives are complete.

## Gate 4 — Performance
Measure before optimizing:
- frame time / FPS
- CPU and GPU frame time
- physics time
- memory
- draw calls
- visible object count

## Gate 5 — Android
- Export toolchain is reproducible.
- Debug build installs on a real device.
- Logs are clean enough to diagnose failures.
- Performance is measured on target hardware.
- Release signing credentials never enter Git.

A gate is not marked passed from source inspection alone; it requires the corresponding reproducible verification.

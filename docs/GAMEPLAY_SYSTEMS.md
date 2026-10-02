# Gameplay Systems

## Traffic violations

Traffic violations use structured evidence instead of passing only a display string between systems.

### Current playable flow

1. A traffic vehicle moves along its route.
2. The vehicle checks its configured speed against the speed limit.
3. A validated speeding evidence object is created once.
4. The evidence is emitted through violation_evidence_detected.
5. The traffic-stop scenario validates and stores that evidence.
6. The mission's stop objective requires both a confirmed violation and valid evidence.
7. The main HUD can display the measured speed and configured limit.
8. After the stop, the player interacts with the civilian to finish the mission.

### Separation of responsibilities

- scripts/violations/traffic_violation.gd owns evidence construction and validation.
- scripts/vehicles/traffic_vehicle.gd owns the physical facts needed to detect speeding.
- scripts/missions/traffic_stop_scenario.gd consumes validated evidence and controls mission progression.
- scripts/core/main.gd composes systems and forwards user-facing feedback to the HUD.
- scripts/ui/status_hud.gd renders mission state.

This keeps mission logic from reaching into unrelated vehicle physics state.

## Adding another violation

A new violation should not be considered playable merely because an enum or data entry exists. It needs a deterministic detector, a documented evidence shape, mission integration, HUD feedback where appropriate, and headless automated coverage.

# Gameplay Systems

## Traffic violations

The traffic system now records structured evidence for the currently playable speeding violation. Evidence contains a stable identifier, human-readable title, observed speed, configured speed limit, and calculated excess speed.

The current playable flow is:
1. The traffic vehicle moves along its route.
2. The configured traffic speed is checked against the speed limit.
3. Speeding is recorded once and emitted as both legacy HUD text and structured evidence.
4. The traffic-stop mission requires the confirmed violation before the stop objective can complete.
5. The player can interact with the civilian after the stop and complete the mission.

Other violation identifiers should not be treated as playable until a deterministic world detector, public state, mission integration, HUD feedback, and automated headless coverage exist.

## Verification

Godot headless tests cover the violation data model and the traffic vehicle's structured evidence. CI also parses all GDScript and smoke-tests the main scene.

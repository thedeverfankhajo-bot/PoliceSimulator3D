# Traffic AI

The starter city uses a lightweight deterministic traffic controller.

## Responsibilities

- Register active traffic vehicles.
- Keep vehicles separated in the same lane.
- Reduce speed as a vehicle approaches a leader.
- Request a full stop at a very small gap.
- Leave traffic-violation evidence to the vehicle and violation systems.

The controller intentionally does not teleport or directly edit a vehicle's transform. Vehicle movement remains inside TrafficVehicle, which uses CharacterBody3D velocity and move_and_slide() during the physics step.

This keeps simulation rules separated from movement and makes the controller easy to test headlessly.

## Next extensions

- lane changes;
- intersection reservations;
- emergency-vehicle yielding;
- pedestrian crossings;
- route selection.

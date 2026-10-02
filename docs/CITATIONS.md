# Citation system

The citation manager is deliberately separated from violation detection.

Flow:

1. TrafficVehicle produces validated evidence.
2. TrafficStopScenario confirms the evidence belongs to the mission target.
3. Main creates a citation from that evidence.
4. The citation is kept in a bounded in-memory history.

A citation is data, not authority: the action field can represent a warning or another gameplay outcome without embedding real-world legal rules.

The history is bounded to 100 records so a long session cannot grow memory without limit.

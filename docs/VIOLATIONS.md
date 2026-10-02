# Traffic violations

Traffic violations are represented as structured evidence instead of only a display string.

## Current violation

### Speeding

The traffic vehicle records:

- stable violation id: `speeding`
- display title: `سرعت غیرمجاز`
- observed speed in km/h
- configured speed limit in km/h
- excess speed in km/h

The violation is emitted once per traffic-vehicle run. Resetting a looping traffic vehicle clears the evidence so a later run can generate a new event.

## Extension rule

New violation types should add a stable identifier and a small evidence-construction/validation path in `scripts/violations/`. Detection should remain in the vehicle or world sensor that has the necessary facts; mission logic should consume evidence through signals rather than reaching into unrelated physics state.

Every new violation type must have:

1. deterministic unit coverage;
2. parser/CI coverage through the repository validation workflow;
3. a documented evidence shape;
4. no dependency on platform-specific input.

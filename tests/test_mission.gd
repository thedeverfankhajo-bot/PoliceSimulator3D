extends SceneTree

const MISSION_SCRIPT := preload("res://scripts/missions/mission.gd")
const MISSION_STATE := preload("res://scripts/missions/mission_state.gd")

func _fail(message: String) -> void:
	push_error(message)
	quit(1)

func _initialize() -> void:
	var mission = MISSION_SCRIPT.new()
	if mission.configure([]):
		_fail("Empty mission objectives must be rejected.")
		return
	if not mission.configure([
		{"id": "a", "title": "Objective A"},
		{"id": "b", "title": "Objective B"}
	]):
		_fail("Valid objectives must be accepted.")
		return
	if mission.configure([
		{"id": "duplicate", "title": "Objective A"},
		{"id": "duplicate", "title": "Objective B"}
	]):
		_fail("Duplicate objective IDs must be rejected.")
		return
	if not mission.start():
		_fail("Configured mission must start.")
		return
	if mission.complete_objective(-1):
		_fail("Negative objective index must be rejected.")
		return
	if not mission.complete_objective(0):
		_fail("First objective should complete.")
		return
	if mission.complete_objective(0):
		_fail("Completed objective must not complete twice.")
		return
	if not mission.complete_objective(1):
		_fail("Second objective should complete.")
		return
	if mission.status != MISSION_STATE.Status.COMPLETED:
		_fail("Mission should complete after all objectives.")
		return
	print("Mission tests passed.")
	quit(0)

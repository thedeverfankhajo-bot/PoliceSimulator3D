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

	var failed_mission = MISSION_SCRIPT.new()
	if not failed_mission.configure([{"id": "fail", "title": "Failure objective"}]):
		_fail("Failure test mission must configure.")
		return
	if not failed_mission.start():
		_fail("Failure test mission must start.")
		return
	if not failed_mission.fail("Traffic suspect escaped"):
		_fail("Active mission must be able to fail.")
		return
	if failed_mission.status != MISSION_STATE.Status.FAILED:
		_fail("Failed mission must enter FAILED state.")
		return
	if failed_mission.failure_reason != "Traffic suspect escaped":
		_fail("Failure reason must be preserved.")
		return
	if failed_mission.fail("second failure"):
		_fail("Failed mission must not fail twice.")
		return

	print("Mission tests passed.")
	quit(0)

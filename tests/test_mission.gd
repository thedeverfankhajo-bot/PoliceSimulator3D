extends SceneTree

const MISSION_SCRIPT := preload("res://scripts/missions/mission.gd")
const MISSION_STATE := preload("res://scripts/missions/mission_state.gd")
const MISSION_MANAGER_SCRIPT := preload("res://scripts/missions/mission_manager.gd")

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

	var manager := MISSION_MANAGER_SCRIPT.new()
	var managed_mission = MISSION_SCRIPT.new()
	if not managed_mission.configure([{"id": "managed", "title": "Managed objective"}]):
		_fail("Managed mission must configure.")
		return
	if not manager.start_mission(managed_mission):
		_fail("Mission manager must start a configured mission.")
		return
	if manager.active_mission != managed_mission:
		_fail("Mission manager must track its active mission.")
		return
	if not managed_mission.complete_objective(0):
		_fail("Managed mission objective should complete.")
		return
	if manager.active_mission != null:
		_fail("Mission manager must clear active mission after completion.")
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

	var failed_managed_mission = MISSION_SCRIPT.new()
	if not failed_managed_mission.configure([{"id": "managed_fail", "title": "Managed failure"}]):
		_fail("Managed failure mission must configure.")
		return
	if not manager.start_mission(failed_managed_mission):
		_fail("Mission manager must start failure mission.")
		return
	if not failed_managed_mission.fail("Suspect escaped"):
		_fail("Managed mission must fail.")
		return
	if manager.active_mission != null:
		_fail("Mission manager must clear active mission after failure.")
		return

	print("Mission tests passed.")
	quit(0)

extends SceneTree

const MISSION_SCRIPT := preload("res://scripts/missions/mission.gd")

func _initialize() -> void:
	var mission: Mission = MISSION_SCRIPT.new()
	assert(not mission.configure([]), "Empty mission objectives must be rejected.")
	assert(mission.configure([
		{"id": "a", "title": "Objective A"},
		{"id": "b", "title": "Objective B"}
	]), "Valid objectives must be accepted.")
	assert(not mission.configure([
		{"id": "duplicate", "title": "Objective A"},
		{"id": "duplicate", "title": "Objective B"}
	]), "Duplicate objective IDs must be rejected.")
	assert(mission.start(), "Configured mission must start.")
	assert(not mission.complete_objective(-1), "Negative objective index must be rejected.")
	assert(mission.complete_objective(0), "First objective should complete.")
	assert(not mission.complete_objective(0), "Completed objective must not complete twice.")
	assert(mission.complete_objective(1), "Second objective should complete.")
	assert(mission.status == MissionState.Status.COMPLETED, "Mission should complete after all objectives.")
	print("Mission tests passed.")
	quit(0)

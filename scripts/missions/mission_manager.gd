extends Node
class_name MissionManager

signal mission_started(mission)
signal mission_completed(mission)
signal mission_failed(mission, reason: String)

var active_mission
var _starting_mission := false

func start_mission(mission) -> bool:
	if _starting_mission or mission == null or is_instance_valid(active_mission):
		return false
	if not mission.completed.is_connected(_on_mission_completed):
		mission.completed.connect(_on_mission_completed, CONNECT_ONE_SHOT)
	if not mission.failed.is_connected(_on_mission_failed):
		mission.failed.connect(_on_mission_failed, CONNECT_ONE_SHOT)

	_starting_mission = true
	active_mission = mission
	var started: bool = mission.start()
	_starting_mission = false
	if not started:
		if active_mission == mission:
			active_mission = null
		if mission.completed.is_connected(_on_mission_completed):
			mission.completed.disconnect(_on_mission_completed)
		if mission.failed.is_connected(_on_mission_failed):
			mission.failed.disconnect(_on_mission_failed)
		return false

	mission_started.emit(mission)
	return true

func _on_mission_completed() -> void:
	if active_mission == null:
		return
	var mission = active_mission
	mission_completed.emit(mission)
	active_mission = null

func _on_mission_failed(reason: String) -> void:
	if active_mission == null:
		return
	var mission = active_mission
	mission_failed.emit(mission, reason)
	active_mission = null

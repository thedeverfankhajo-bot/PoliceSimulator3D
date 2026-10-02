extends Node
class_name MissionManager

signal mission_started(mission)
signal mission_completed(mission)

var active_mission

func start_mission(mission) -> bool:
	if mission == null or is_instance_valid(active_mission):
		return false
	if not mission.start():
		return false
	active_mission = mission
	mission.completed.connect(_on_mission_completed.bind(mission), CONNECT_ONE_SHOT)
	mission_started.emit(mission)
	return true

func _on_mission_completed(mission) -> void:
	if active_mission != mission:
		return
	mission_completed.emit(mission)
	active_mission = null

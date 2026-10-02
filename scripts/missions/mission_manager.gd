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
	if not mission.completed.is_connected(_on_mission_completed):
		mission.completed.connect(_on_mission_completed, CONNECT_ONE_SHOT)
	mission_started.emit(mission)
	return true

func _on_mission_completed(mission) -> void:
	if active_mission != mission:
		return
	mission_completed.emit(mission)
	active_mission = null

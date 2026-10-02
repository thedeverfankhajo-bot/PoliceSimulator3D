extends Node
class_name MissionManager

signal mission_started(mission: Mission)
signal mission_completed(mission: Mission)

var active_mission: Mission

func start_mission(mission: Mission) -> bool:
	if active_mission != null:
		return false
	active_mission = mission
	mission.completed.connect(_on_mission_completed.bind(mission))
	mission.start()
	mission_started.emit(mission)
	return true

func _on_mission_completed(mission: Mission) -> void:
	if active_mission == mission:
		mission_completed.emit(mission)
		active_mission = null

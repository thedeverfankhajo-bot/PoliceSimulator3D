extends Node
class_name MissionManager

signal mission_started(mission: Mission)
signal mission_completed(mission: Mission)

var active_mission: Mission

func start_mission(mission: Mission) -> bool:
	if mission == null or active_mission != null:
		return false
	active_mission = mission
	mission.completed.connect(_on_mission_completed.bind(mission), CONNECT_ONE_SHOT)
	mission.start()
	if mission.status != MissionState.Status.ACTIVE:
		active_mission = null
		return false
	mission_started.emit(mission)
	return true

func _on_mission_completed(mission: Mission) -> void:
	if active_mission != mission:
		return
	mission_completed.emit(mission)
	active_mission = null

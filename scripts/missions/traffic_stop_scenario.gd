extends Node
class_name TrafficStopScenario

@export var mission_title := "اولین توقف ترافیکی"
@export_multiline var mission_description := "سوار خودروی پلیس شو، به شهروند نزدیک شو و با او تعامل کن."

var mission
var vehicle: Node
var npc: Node

func setup(target_vehicle: Node, target_npc: Node) -> bool:
	if target_vehicle == null or target_npc == null:
		return false
	vehicle = target_vehicle
	npc = target_npc
	mission = preload("res://scripts/missions/mission.gd").new()
	mission.mission_id = "traffic_stop_001"
	mission.title = mission_title
	mission.description = mission_description
	return mission.configure([
		{"id": "enter_patrol_vehicle", "title": "سوار خودروی پلیس شو"},
		{"id": "talk_to_civilian", "title": "با شهروند صحبت کن"}
	])

func start(mission_manager: Node) -> bool:
	if mission == null or mission_manager == null:
		return false
	if not mission_manager.start_mission(mission):
		return false
	if not vehicle.entered.is_connected(_on_vehicle_entered):
		vehicle.entered.connect(_on_vehicle_entered)
	if not npc.interacted.is_connected(_on_npc_interacted):
		npc.interacted.connect(_on_npc_interacted)
	if not mission.completed.is_connected(_on_mission_completed):
		mission.completed.connect(_on_mission_completed, CONNECT_ONE_SHOT)
	return true

func _on_vehicle_entered(_vehicle) -> void:
	if mission == null or mission.status != preload("res://scripts/missions/mission_state.gd").Status.ACTIVE:
		return
	mission.complete_objective(0)

func _on_npc_interacted(_npc) -> void:
	if mission == null or mission.status != preload("res://scripts/missions/mission_state.gd").Status.ACTIVE:
		return
	if not mission.is_objective_completed(0):
		return
	mission.complete_objective(1)

func _on_mission_completed() -> void:
	if is_instance_valid(npc) and npc.interacted.is_connected(_on_npc_interacted):
		npc.interacted.disconnect(_on_npc_interacted)
	if is_instance_valid(vehicle) and vehicle.entered.is_connected(_on_vehicle_entered):
		vehicle.entered.disconnect(_on_vehicle_entered)

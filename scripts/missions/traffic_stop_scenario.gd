extends Node
class_name TrafficStopScenario

@export var mission_title := "اولین توقف ترافیکی"
@export_multiline var mission_description := "سوار خودروی پلیس شو، خودروی متخلف را متوقف کن و با شهروند تعامل کن."

const MISSION_SCRIPT := preload("res://scripts/missions/mission.gd")
const MISSION_STATE := preload("res://scripts/missions/mission_state.gd")

var mission
var vehicle: Node
var traffic_vehicle: Node
var npc: Node

func setup(target_vehicle: Node, target_traffic_vehicle: Node, target_npc: Node) -> bool:
	if target_vehicle == null or target_traffic_vehicle == null or target_npc == null:
		return false
	if not target_vehicle.has_signal("entered"):
		return false
	if not target_traffic_vehicle.has_signal("stopped"):
		return false
	if not target_npc.has_signal("interacted"):
		return false
	vehicle = target_vehicle
	traffic_vehicle = target_traffic_vehicle
	npc = target_npc
	mission = MISSION_SCRIPT.new()
	mission.mission_id = "traffic_stop_001"
	mission.title = mission_title
	mission.description = mission_description
	return mission.configure([
		{"id": "enter_patrol_vehicle", "title": "سوار خودروی پلیس شو"},
		{"id": "stop_traffic_vehicle", "title": "خودروی متخلف را متوقف کن"},
		{"id": "talk_to_civilian", "title": "با شهروند صحبت کن"}
	])

func start(mission_manager: Node) -> bool:
	if mission == null or mission_manager == null:
		return false
	if not mission_manager.start_mission(mission):
		return false
	if not vehicle.entered.is_connected(_on_vehicle_entered):
		vehicle.entered.connect(_on_vehicle_entered)
	if not traffic_vehicle.stopped.is_connected(_on_traffic_vehicle_stopped):
		traffic_vehicle.stopped.connect(_on_traffic_vehicle_stopped)
	if not npc.interacted.is_connected(_on_npc_interacted):
		npc.interacted.connect(_on_npc_interacted)
	return true

func _on_vehicle_entered(_vehicle) -> void:
	if not _is_active():
		return
	mission.complete_objective(0)

func _on_traffic_vehicle_stopped(_vehicle) -> void:
	if not _is_active() or not mission.is_objective_completed(0):
		return
	mission.complete_objective(1)

func _on_npc_interacted(_npc) -> void:
	if not _is_active() or not mission.is_objective_completed(1):
		return
	mission.complete_objective(2)

func _is_active() -> bool:
	return mission != null and mission.status == MISSION_STATE.Status.ACTIVE

func _on_mission_completed() -> void:
	if is_instance_valid(npc) and npc.interacted.is_connected(_on_npc_interacted):
		npc.interacted.disconnect(_on_npc_interacted)
	if is_instance_valid(traffic_vehicle) and traffic_vehicle.stopped.is_connected(_on_traffic_vehicle_stopped):
		traffic_vehicle.stopped.disconnect(_on_traffic_vehicle_stopped)
	if is_instance_valid(vehicle) and vehicle.entered.is_connected(_on_vehicle_entered):
		vehicle.entered.disconnect(_on_vehicle_entered)

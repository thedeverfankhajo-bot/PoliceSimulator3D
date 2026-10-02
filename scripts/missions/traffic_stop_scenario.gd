extends Node
class_name TrafficStopScenario

@export var mission_title := "اولین توقف ترافیکی"
@export_multiline var mission_description := "سوار خودروی پلیس شو، خودروی متخلف را متوقف کن و با شهروند تعامل کن."
@export_range(30.0, 600.0, 5.0) var mission_timeout_seconds: float = 180.0

const MISSION_SCRIPT := preload("res://scripts/missions/mission.gd")
const MISSION_STATE := preload("res://scripts/missions/mission_state.gd")

var mission
var vehicle: Node
var traffic_vehicle: Node
var npc: Node
var _mission_timer: Timer

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
	var objectives: Array[Dictionary] = [
		{"id": "enter_patrol_vehicle", "title": "سوار خودروی پلیس شو"},
		{"id": "stop_traffic_vehicle", "title": "خودروی متخلف را متوقف کن"},
		{"id": "talk_to_civilian", "title": "با شهروند صحبت کن"}
	]
	return mission.configure(objectives)

func start(mission_manager: Node) -> bool:
	if mission == null or mission_manager == null:
		return false
	if not is_instance_valid(vehicle) or not is_instance_valid(traffic_vehicle) or not is_instance_valid(npc):
		return false
	if not vehicle.entered.is_connected(_on_vehicle_entered):
		vehicle.entered.connect(_on_vehicle_entered)
	if not traffic_vehicle.stopped.is_connected(_on_traffic_vehicle_stopped):
		traffic_vehicle.stopped.connect(_on_traffic_vehicle_stopped)
	if not mission.completed.is_connected(_on_mission_completed):
		mission.completed.connect(_on_mission_completed, CONNECT_ONE_SHOT)
	if not mission.failed.is_connected(_on_mission_failed):
		mission.failed.connect(_on_mission_failed, CONNECT_ONE_SHOT)
	if not npc.interacted.is_connected(_on_npc_interacted):
		npc.interacted.connect(_on_npc_interacted)
	if not mission_manager.start_mission(mission):
		_disconnect_signals()
		return false
	_mission_timer = Timer.new()
	_mission_timer.one_shot = true
	_mission_timer.wait_time = mission_timeout_seconds
	_mission_timer.timeout.connect(_on_mission_timeout)
	add_child(_mission_timer)
	_mission_timer.start()
	return true

func _on_vehicle_entered(entered_vehicle) -> void:
	if not _is_active() or entered_vehicle != vehicle:
		return
	mission.complete_objective(0)

func _on_traffic_vehicle_stopped(stopped_vehicle) -> void:
	if not _is_active() or not mission.is_objective_completed(0):
		return
	if stopped_vehicle != traffic_vehicle:
		return
	mission.complete_objective(1)

func _on_npc_interacted(interacted_npc) -> void:
	if not _is_active() or not mission.is_objective_completed(1):
		return
	if interacted_npc != npc:
		return
	mission.complete_objective(2)

func _on_mission_failed(_reason: String) -> void:
	_disconnect_signals()
	_stop_timer()

func _on_mission_timeout() -> void:
	if _is_active():
		mission.fail("زمان مأموریت به پایان رسید.")

func _is_active() -> bool:
	return mission != null and mission.status == MISSION_STATE.Status.ACTIVE

func _on_mission_completed() -> void:
	_disconnect_signals()
	_stop_timer()

func _stop_timer() -> void:
	if is_instance_valid(_mission_timer):
		_mission_timer.stop()
		_mission_timer.queue_free()
		_mission_timer = null

func _disconnect_signals() -> void:
	if is_instance_valid(npc) and npc.interacted.is_connected(_on_npc_interacted):
		npc.interacted.disconnect(_on_npc_interacted)
	if is_instance_valid(traffic_vehicle) and traffic_vehicle.stopped.is_connected(_on_traffic_vehicle_stopped):
		traffic_vehicle.stopped.disconnect(_on_traffic_vehicle_stopped)
	if is_instance_valid(vehicle) and vehicle.entered.is_connected(_on_vehicle_entered):
		vehicle.entered.disconnect(_on_vehicle_entered)

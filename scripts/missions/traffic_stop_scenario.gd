extends Node
class_name TrafficStopScenario

@export var mission_title := "اولین توقف ترافیکی"
@export_multiline var mission_description := "خودرو را بررسی کن و سپس با شهروند تعامل کن."

var mission: Mission
var vehicle: PoliceVehicle
var npc: CivilianNPC

func setup(target_vehicle: PoliceVehicle, target_npc: CivilianNPC) -> bool:
	if target_vehicle == null or target_npc == null:
		return false
	vehicle = target_vehicle
	npc = target_npc
	mission = Mission.new()
	mission.mission_id = "traffic_stop_001"
	mission.title = mission_title
	mission.description = mission_description
	return mission.configure([
		{"id": "inspect_vehicle", "title": "با خودرو تعامل کن"},
		{"id": "talk_to_civilian", "title": "با شهروند صحبت کن"}
	])

func start(mission_manager: MissionManager) -> bool:
	if mission == null or mission_manager == null:
		return false
	if not mission_manager.start_mission(mission):
		return false
	vehicle.entered.connect(_on_vehicle_interacted, CONNECT_ONE_SHOT)
	npc.interacted.connect(_on_npc_interacted, CONNECT_ONE_SHOT)
	return true

func _on_vehicle_interacted(_vehicle: PoliceVehicle) -> void:
	if mission != null:
		mission.complete_objective(0)

func _on_npc_interacted(_npc: CivilianNPC) -> void:
	if mission != null:
		mission.complete_objective(1)

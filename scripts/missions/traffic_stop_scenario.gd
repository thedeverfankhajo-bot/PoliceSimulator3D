extends Node
class_name TrafficStopScenario

@export var mission_title := "اولین توقف ترافیکی"
@export_multiline var mission_description := "خودرو را بررسی کن و سپس با شهروند تعامل کن."

var mission: Mission
var vehicle: PoliceVehicle
var npc: CivilianNPC

func setup(target_vehicle: PoliceVehicle, target_npc: CivilianNPC) -> void:
	vehicle = target_vehicle
	npc = target_npc
	mission = Mission.new()
	mission.mission_id = "traffic_stop_001"
	mission.title = mission_title
	mission.description = mission_description
	mission.configure([
		{"id": "inspect_vehicle", "title": "با خودرو تعامل کن"},
		{"id": "talk_to_civilian", "title": "با شهروند صحبت کن"}
	])
	mission.start()
	vehicle.entered.connect(_on_vehicle_interacted)
	npc.interacted.connect(_on_npc_interacted)

func _on_vehicle_interacted(_vehicle: PoliceVehicle) -> void:
	mission.complete_objective(0)

func _on_npc_interacted(_npc: CivilianNPC) -> void:
	mission.complete_objective(1)

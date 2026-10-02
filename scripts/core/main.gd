extends Node3D
## Main world composition. Gameplay rules stay in isolated systems.

const PLAYER_SCENE := preload("res://scenes/player/player.tscn")
const VEHICLE_SCENE := preload("res://scenes/vehicles/police_vehicle.tscn")
const NPC_SCENE := preload("res://scenes/npcs/civilian_npc.tscn")
const TRAFFIC_VEHICLE_SCENE := preload("res://scenes/vehicles/traffic_vehicle.tscn")
const SCENARIO_SCRIPT := preload("res://scripts/missions/traffic_stop_scenario.gd")

@onready var player_spawn: Marker3D = $PlayerSpawn
@onready var hud: StatusHud = $StatusHUD

var mission_manager: MissionManager
var player: PlayerController

func _ready() -> void:
	player = PLAYER_SCENE.instantiate()
	player.global_position = player_spawn.global_position
	add_child(player)
	player.vehicle_exit_blocked.connect(_on_vehicle_exit_blocked)

	var vehicle: PoliceVehicle = VEHICLE_SCENE.instantiate()
	vehicle.global_position = Vector3(0, 0, -8)
	add_child(vehicle)
	vehicle.entered.connect(player.enter_vehicle)

	var npc: CivilianNPC = NPC_SCENE.instantiate()
	npc.global_position = Vector3(3, 0, -8)
	add_child(npc)

	var traffic_vehicle: TrafficVehicle = TRAFFIC_VEHICLE_SCENE.instantiate()
	traffic_vehicle.global_position = Vector3(0, 0.65, 18)
	add_child(traffic_vehicle)
	traffic_vehicle.violation_detected.connect(_on_traffic_violation)

	mission_manager = MissionManager.new()
	add_child(mission_manager)
	mission_manager.mission_completed.connect(_on_mission_completed)

	var scenario := SCENARIO_SCRIPT.new()
	add_child(scenario)
	if not scenario.setup(vehicle, npc):
		push_error("Traffic stop scenario failed to configure.")
		scenario.queue_free()
		return
	if not scenario.start(mission_manager):
		push_error("Traffic stop scenario failed to start.")
		return
	hud.show_mission(scenario.mission)

func _on_mission_completed(mission: Mission) -> void:
	hud.set_status("ماموریت کامل شد: %s" % mission.title)

func _on_vehicle_exit_blocked() -> void:
	push_warning("Vehicle exit blocked: no safe space for the player.")

func _on_traffic_violation(_vehicle: TrafficVehicle, violation: String) -> void:
	hud.set_status("تخلف ثبت شد: %s" % violation)

extends Node3D
## Main world composition. Gameplay rules stay in isolated systems.

const PLAYER_SCENE := preload("res://scenes/player/player.tscn")
const VEHICLE_SCENE := preload("res://scenes/vehicles/police_vehicle.tscn")
const NPC_SCENE := preload("res://scenes/npcs/civilian_npc.tscn")
const SCENARIO_SCRIPT := preload("res://scripts/missions/traffic_stop_scenario.gd")

@onready var player_spawn: Marker3D = $PlayerSpawn
@onready var hud: StatusHud = $StatusHUD

func _ready() -> void:
	var player := PLAYER_SCENE.instantiate()
	player.global_position = player_spawn.global_position
	add_child(player)

	var vehicle: PoliceVehicle = VEHICLE_SCENE.instantiate()
	vehicle.global_position = Vector3(0, 0, -8)
	add_child(vehicle)

	var npc: CivilianNPC = NPC_SCENE.instantiate()
	npc.global_position = Vector3(3, 0, -8)
	add_child(npc)

	var scenario := SCENARIO_SCRIPT.new()
	add_child(scenario)
	scenario.setup(vehicle, npc)
	hud.show_mission(scenario.mission)

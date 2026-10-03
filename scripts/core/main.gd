extends Node3D
## Main world composition. Gameplay rules stay in isolated systems.
## The environment is deliberately procedural so the repository stays small and
## the Android debug build has a useful playable scene without external assets.

const PLAYER_SCENE := preload("res://scenes/player/player.tscn")
const VEHICLE_SCENE := preload("res://scenes/vehicles/police_vehicle.tscn")
const NPC_SCENE := preload("res://scenes/npcs/civilian_npc.tscn")
const TRAFFIC_VEHICLE_SCENE := preload("res://scenes/vehicles/traffic_vehicle.tscn")
const SCENARIO_SCRIPT := preload("res://scripts/missions/traffic_stop_scenario.gd")
const TRAFFIC_LIGHT_SCRIPT := preload("res://scripts/traffic/traffic_light.gd")
const TRAFFIC_AI_SCRIPT := preload("res://scripts/traffic/traffic_ai_controller.gd")
const CAREER_SCRIPT := preload("res://scripts/core/career_progression.gd")
const CITY_EXPANSION_SCRIPT := preload("res://scripts/world/city_expansion.gd")

@onready var player_spawn: Marker3D = $PlayerSpawn
@onready var hud: CanvasLayer = $StatusHUD
@onready var main_menu: CanvasLayer = $MainMenu

var mission_manager: Node
var player: CharacterBody3D
var traffic_ai: Node
var career
var scenario
var _autosave_timer: Timer

func _ready() -> void:
	career = CAREER_SCRIPT.new()
	var city_expansion := CITY_EXPANSION_SCRIPT.new()
	city_expansion.name = "CityVisualExpansion"
	add_child(city_expansion)
	_build_city()
	_spawn_gameplay()
	main_menu.start_requested.connect(_start_new_game)
	main_menu.continue_requested.connect(_continue_game)
	main_menu.tutorial_requested.connect(_on_tutorial_opened)
	main_menu.settings_requested.connect(_on_settings_opened)
	_setup_autosave()

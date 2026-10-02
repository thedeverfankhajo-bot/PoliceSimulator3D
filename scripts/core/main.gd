extends Node3D
## Main world composition. Gameplay rules stay in isolated systems.
## The environment is deliberately procedural so the repository stays small and
## the Android debug build has a useful playable scene without external assets.

const PLAYER_SCENE := preload("res://scenes/player/player.tscn")
const VEHICLE_SCENE := preload("res://scenes/vehicles/police_vehicle.tscn")
const NPC_SCENE := preload("res://scenes/npcs/civilian_npc.tscn")
const TRAFFIC_VEHICLE_SCENE := preload("res://scenes/vehicles/traffic_vehicle.tscn")
const SCENARIO_SCRIPT := preload("res://scripts/missions/traffic_stop_scenario.gd")

@onready var player_spawn: Marker3D = $PlayerSpawn
@onready var hud: CanvasLayer = $StatusHUD

var mission_manager: Node
var player: CharacterBody3D

func _ready() -> void:
	_build_city()
	_spawn_gameplay()

func _spawn_gameplay() -> void:
	player = PLAYER_SCENE.instantiate()
	add_child(player)
	player.global_position = player_spawn.global_position
	player.vehicle_exit_blocked.connect(_on_vehicle_exit_blocked)

	var vehicle = VEHICLE_SCENE.instantiate()
	add_child(vehicle)
	vehicle.global_position = Vector3(0, 0, -8)
	vehicle.entered.connect(player.enter_vehicle)

	var npc = NPC_SCENE.instantiate()
	add_child(npc)
	npc.global_position = Vector3(3, 0, -8)

	var traffic_vehicle = TRAFFIC_VEHICLE_SCENE.instantiate()
	add_child(traffic_vehicle)
	traffic_vehicle.global_position = Vector3(0, 0.65, 18)
	traffic_vehicle.violation_detected.connect(_on_traffic_violation)

	mission_manager = preload("res://scripts/missions/mission_manager.gd").new()
	add_child(mission_manager)
	mission_manager.mission_completed.connect(_on_mission_completed)
	mission_manager.mission_failed.connect(_on_mission_failed)

	var scenario := SCENARIO_SCRIPT.new()
	add_child(scenario)
	if not scenario.setup(vehicle, traffic_vehicle, npc):
		push_error("Traffic stop scenario failed to configure.")
		scenario.queue_free()
		return
	scenario.violation_evidence_confirmed.connect(_on_violation_evidence_confirmed)
	if not scenario.start(mission_manager):
		push_error("Traffic stop scenario failed to start.")
		return
	hud.show_mission(scenario.mission)
	hud.set_status("مأموریت: خودروی پلیس را پیدا کن و سوار شو.")

func _build_city() -> void:
	var road_material := _material(Color(0.055, 0.06, 0.07), 0.92)
	var sidewalk_material := _material(Color(0.38, 0.39, 0.37), 0.88)
	var lane_material := _material(Color(0.88, 0.82, 0.58), 0.65)
	var building_materials := [
		_material(Color(0.26, 0.30, 0.36), 0.78),
		_material(Color(0.34, 0.28, 0.24), 0.82),
		_material(Color(0.22, 0.34, 0.31), 0.8),
		_material(Color(0.36, 0.33, 0.29), 0.86)
	]

	_add_static_box("MainRoad", Vector3(9.0, 0.04, 70.0), Vector3(0, 0.02, -3), road_material)
	_add_static_box("SidewalkLeft", Vector3(3.0, 0.12, 70.0), Vector3(-7.0, 0.08, -3), sidewalk_material)
	_add_static_box("SidewalkRight", Vector3(3.0, 0.12, 70.0), Vector3(7.0, 0.08, -3), sidewalk_material)

	for z in range(-34, 29, 6):
		_add_static_box("LaneMark", Vector3(0.12, 0.025, 2.8), Vector3(0, 0.07, z), lane_material)

	for z in [-27.0, -15.0, -3.0, 9.0, 21.0]:
		_add_crosswalk(z, lane_material)

	var building_data := [
		[Vector3(-12, 3.0, -25), Vector3(6, 6, 8)],
		[Vector3(12, 4.0, -25), Vector3(6, 8, 8)],
		[Vector3(-12, 4.5, -9), Vector3(6, 9, 7)],
		[Vector3(12, 3.2, -9), Vector3(6, 6.4, 7)],
		[Vector3(-12, 5.0, 8), Vector3(6, 10, 9)],
		[Vector3(12, 3.5, 8), Vector3(6, 7, 9)],
		[Vector3(-12, 3.8, 25), Vector3(6, 7.6, 8)],
		[Vector3(12, 4.8, 25), Vector3(6, 9.6, 8)]
	]
	for i in building_data.size():
		var data = building_data[i]
		_add_static_box("Building_%02d" % i, data[1], data[0], building_materials[i % building_materials.size()])

	for z in [-22.0, -10.0, 2.0, 14.0, 26.0]:
		_add_street_light(Vector3(-5.7, 0, z))
		_add_street_light(Vector3(5.7, 0, z))

	_add_sign(Vector3(4.8, 1.2, -2.8), "POLICE STOP")
	_add_sign(Vector3(-4.8, 1.2, 10.0), "50")

func _add_crosswalk(z: float, material: Material) -> void:
	for x in [-3.2, -1.9, -0.6, 0.7, 2.0, 3.3]:
		_add_static_box("Crosswalk", Vector3(0.8, 0.025, 1.8), Vector3(x, 0.08, z), material, false)

func _add_static_box(node_name: String, box_size: Vector3, position: Vector3, material: Material, collision := true) -> Node3D:
	var holder: Node3D
	if collision:
		var body := StaticBody3D.new()
		body.name = node_name
		add_child(body)
		holder = body
		var shape := CollisionShape3D.new()
		var box_shape := BoxShape3D.new()
		box_shape.size = box_size
		shape.shape = box_shape
		body.add_child(shape)
	else:
		holder = Node3D.new()
		holder.name = node_name
		add_child(holder)

	var mesh := MeshInstance3D.new()
	var box_mesh := BoxMesh.new()
	box_mesh.size = box_size
	mesh.mesh = box_mesh
	mesh.material_override = material
	holder.add_child(mesh)
	holder.position = position
	return holder

func _add_street_light(position: Vector3) -> void:
	var pole_material := _material(Color(0.08, 0.09, 0.1), 0.8)
	_add_static_box("LampPost", Vector3(0.12, 3.8, 0.12), position + Vector3(0, 1.9, 0), pole_material)
	var lamp := OmniLight3D.new()
	lamp.position = position + Vector3(0, 3.7, 0)
	lamp.omni_range = 7.0
	lamp.light_energy = 1.4
	lamp.shadow_enabled = true
	add_child(lamp)

func _add_sign(position: Vector3, label_text: String) -> void:
	var pole_material := _material(Color(0.18, 0.19, 0.2), 0.75)
	_add_static_box("SignPole", Vector3(0.08, 2.4, 0.08), position - Vector3(0, 1.2, 0), pole_material)
	var sign_material := _material(Color(0.92, 0.92, 0.84), 0.7)
	_add_static_box("Sign", Vector3(0.9, 0.65, 0.08), position, sign_material)

func _material(color: Color, roughness: float) -> StandardMaterial3D:
	var material := StandardMaterial3D.new()
	material.albedo_color = color
	material.roughness = roughness
	return material

func _on_mission_completed(mission) -> void:
	hud.show_mission(mission)
	hud.set_status("ماموریت کامل شد: %s" % mission.title)

func _on_mission_failed(mission, reason: String) -> void:
	hud.show_mission(mission)
	hud.set_status("ماموریت ناموفق: %s%s" % [mission.title, (" — " + reason) if not reason.is_empty() else ""])

func _on_vehicle_exit_blocked() -> void:
	hud.set_status("خروج از خودرو ممکن نیست؛ مسیر کنار خودرو را باز کن.")

func _on_traffic_violation(_vehicle, violation: String) -> void:
	hud.set_status("تخلف ثبت شد: %s" % violation)

func _on_violation_evidence_confirmed(evidence: Dictionary) -> void:
	var observed := float(evidence.get("observed_speed_kmh", 0.0))
	var limit := float(evidence.get("speed_limit_kmh", 0.0))
	hud.set_status("مدرک تخلف تأیید شد: %s — %.1f / %.1f km/h" % [
		String(evidence.get("title", "تخلف")),
		observed,
		limit
	])

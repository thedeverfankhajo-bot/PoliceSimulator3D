extends CharacterBody3D
class_name PlayerController

signal vehicle_entered(vehicle: Node)
signal vehicle_exited(vehicle: Node)
signal vehicle_exit_blocked()

@export var move_speed: float = 5.0
@export var acceleration: float = 20.0
@export var gravity: float = 18.0
@export var mouse_sensitivity: float = 0.0025

@onready var camera_pivot: Node3D = $CameraPivot
@onready var camera: Camera3D = $CameraPivot/Camera3D
@onready var interaction_detector: Node3D = $CameraPivot/InteractionDetector
@onready var collision_shape: CollisionShape3D = $CollisionShape3D
@onready var visual: Node3D = $Visual

var _pitch := 0.0
var _active_vehicle: Node
var _saved_collision_layer := 1
var _saved_collision_mask := 1
var _saved_camera_pivot_position := Vector3.ZERO

func _ready() -> void:
	Input.mouse_mode = Input.MOUSE_MODE_CAPTURED
	_saved_collision_layer = collision_layer
	_saved_collision_mask = collision_mask
	_saved_camera_pivot_position = camera_pivot.position

func _unhandled_input(event: InputEvent) -> void:
	if event is InputEventMouseMotion and Input.mouse_mode == Input.MOUSE_MODE_CAPTURED:
		rotate_y(-event.relative.x * mouse_sensitivity)
		_pitch = clamp(_pitch - event.relative.y * mouse_sensitivity, deg_to_rad(-75.0), deg_to_rad(75.0))
		camera_pivot.rotation.x = _pitch
	elif event is InputEventKey and event.pressed and not event.echo and event.keycode == KEY_ESCAPE:
		Input.mouse_mode = Input.MOUSE_MODE_VISIBLE

func _physics_process(delta: float) -> void:
	if _active_vehicle != null:
		if Input.is_action_just_pressed("interact"):
			exit_vehicle()
		return

	if Input.mouse_mode != Input.MOUSE_MODE_CAPTURED:
		if Input.is_action_just_pressed("interact"):
			Input.mouse_mode = Input.MOUSE_MODE_CAPTURED
		return

	var input_vector := Input.get_vector("move_left", "move_right", "move_forward", "move_backward")
	var direction := (transform.basis * Vector3(input_vector.x, 0.0, input_vector.y)).normalized()
	var target_velocity := direction * move_speed
	velocity.x = move_toward(velocity.x, target_velocity.x, acceleration * delta)
	velocity.z = move_toward(velocity.z, target_velocity.z, acceleration * delta)

	if not is_on_floor():
		velocity.y -= gravity * delta
	else:
		velocity.y = 0.0

	move_and_slide()

func enter_vehicle(vehicle: Node) -> void:
	if vehicle == null or _active_vehicle != null or not is_instance_valid(vehicle):
		return
	if vehicle.is_occupied:
		return

	_active_vehicle = vehicle
	_active_vehicle.set_occupied(true)

	interaction_detector.set_physics_process(false)
	visual.visible = false
	set_collision_layer(0)
	set_collision_mask(0)
	velocity = Vector3.ZERO

	reparent(vehicle, true)
	position = Vector3.ZERO
	camera_pivot.position = Vector3(0.0, 1.65, 5.5)
	camera_pivot.rotation.x = deg_to_rad(-10.0)
	camera.make_current()

	vehicle_entered.emit(vehicle)

func exit_vehicle() -> void:
	if _active_vehicle == null or not is_instance_valid(_active_vehicle):
		_active_vehicle = null
		return

	var vehicle := _active_vehicle
	var exit_position: Vector3 = vehicle.get_exit_position()
	if not _is_exit_position_clear(exit_position, vehicle):
		vehicle_exit_blocked.emit()
		return

	var world_parent := vehicle.get_parent()
	if world_parent == null:
		return

	_active_vehicle = null
	vehicle.set_occupied(false)
	reparent(world_parent, true)
	global_position = exit_position
	global_rotation = Vector3(0.0, vehicle.global_rotation.y, 0.0)
	reset_physics_interpolation()

	set_collision_layer(_saved_collision_layer)
	set_collision_mask(_saved_collision_mask)
	visual.visible = true
	interaction_detector.set_physics_process(true)
	camera_pivot.position = _saved_camera_pivot_position
	camera_pivot.rotation.x = _pitch
	camera.make_current()
	vehicle_exited.emit(vehicle)

func _is_exit_position_clear(exit_position: Vector3, vehicle: Node) -> bool:
	if collision_shape.shape == null:
		return false

	var query := PhysicsShapeQueryParameters3D.new()
	query.shape = collision_shape.shape
	query.transform = Transform3D(Basis.IDENTITY, exit_position + Vector3.UP * 0.9)
	query.collision_mask = _saved_collision_mask
	query.collide_with_bodies = true
	query.collide_with_areas = false
	query.exclude = [get_rid(), vehicle.get_rid()]

	var hits := get_world_3d().direct_space_state.intersect_shape(query, 8)
	return hits.is_empty()

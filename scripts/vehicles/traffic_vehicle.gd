extends CharacterBody3D
class_name TrafficVehicle

const TRAFFIC_VIOLATION_SCRIPT := preload("res://scripts/violations/traffic_violation.gd")


signal violation_detected(vehicle: TrafficVehicle, violation: String)
signal violation_evidence_detected(vehicle: TrafficVehicle, evidence: Dictionary)
signal stopped(vehicle: TrafficVehicle)

@export var vehicle_id := "civilian_sedan_001"
@export_range(0.0, 100.0, 1.0) var traffic_speed_kmh: float = 54.0
@export_range(0.0, 100.0, 1.0) var speed_limit_kmh: float = 50.0
@export var loop_start_z: float = 18.0
@export var loop_end_z: float = -24.0
@export var waypoint_reach_distance: float = 1.5
@export var turn_speed: float = 5.0
@export var waypoints: Array[Node3D] = []
@export var traffic_light: Node
@export var obey_traffic_light := false

var _waypoint_index := 0

var is_stopped := false
var _violation_reported := false
var _violation_evidence: Dictionary = {}
var _loop_start_position := Vector3.ZERO
var _loop_reset_rotation := Basis.IDENTITY
var _spawn_initialized := false
var _last_z := 0.0
var _red_light_reported := false


func _ready() -> void:
	_loop_start_position = global_position
	_loop_reset_rotation = global_transform.basis
	_last_z = global_position.z

func _physics_process(delta: float) -> void:
	var previous_z := global_position.z
	if not _spawn_initialized:
		_loop_start_position = global_position
		_loop_reset_rotation = global_transform.basis
		_spawn_initialized = true
	if is_stopped:
		velocity = Vector3.ZERO
		return
	if waypoints.is_empty():
		velocity = get_traffic_velocity()
		move_and_slide()
		_check_violation()
		_check_red_light_violation(previous_z)
		if global_position.z <= loop_end_z:
			_reset_to_loop_start()
		return
	_follow_waypoints(delta)
	_check_violation()
	_check_red_light_violation(previous_z)

func _follow_waypoints(delta: float) -> void:
	if _waypoint_index >= waypoints.size():
		_waypoint_index = 0
	var target := waypoints[_waypoint_index]
	if not is_instance_valid(target):
		waypoints.remove_at(_waypoint_index)
		if waypoints.is_empty():
			return
		_waypoint_index %= waypoints.size()
		target = waypoints[_waypoint_index]
	var offset := target.global_position - global_position
	offset.y = 0.0
	if offset.length() <= waypoint_reach_distance:
		_waypoint_index = (_waypoint_index + 1) % waypoints.size()
		target = waypoints[_waypoint_index]
		offset = target.global_position - global_position
		offset.y = 0.0
	if offset.length_squared() <= 0.001:
		velocity = Vector3.ZERO
		return
	var direction := offset.normalized()
	velocity = direction * (traffic_speed_kmh / 3.6)
	var target_yaw := atan2(-direction.x, -direction.z)
	rotation.y = lerp_angle(rotation.y, target_yaw, clamp(turn_speed * delta, 0.0, 1.0))
	move_and_slide()

func _reset_to_loop_start() -> void:
	var reset_position := _loop_start_position
	reset_position.z = loop_start_z
	global_position = reset_position
	global_transform.basis = _loop_reset_rotation
	_violation_reported = false
	_violation_evidence.clear()
	_red_light_reported = false
	velocity = get_traffic_velocity()

func get_traffic_velocity() -> Vector3:
	return -transform.basis.z * (traffic_speed_kmh / 3.6)

func _check_violation() -> void:
	if _violation_reported or traffic_speed_kmh <= speed_limit_kmh:
		return
	_violation_reported = true
	_violation_evidence = TRAFFIC_VIOLATION_SCRIPT.create_speeding_evidence(traffic_speed_kmh, speed_limit_kmh)
	if not TRAFFIC_VIOLATION_SCRIPT.is_valid_evidence(_violation_evidence):
		_violation_evidence.clear()
		_violation_reported = false
		return
	violation_detected.emit(self, String(_violation_evidence["title"]))
	violation_evidence_detected.emit(self, _violation_evidence.duplicate(true))

func _check_red_light_violation(previous_z: float) -> void:
	if _red_light_reported or traffic_light == null or not is_instance_valid(traffic_light):
		return
	if not traffic_light.has_method("is_red") or not traffic_light.is_red():
		return
	if not traffic_light.has_method("get_stop_line_z"):
		return
	var stop_line_z := float(traffic_light.get_stop_line_z())
	if previous_z > stop_line_z and global_position.z <= stop_line_z and not obey_traffic_light:
		_red_light_reported = true
		_violation_reported = true
		_violation_evidence = TRAFFIC_VIOLATION_SCRIPT.create_red_light_evidence(traffic_light.get_light_id(), stop_line_z)
		if not TRAFFIC_VIOLATION_SCRIPT.is_valid_evidence(_violation_evidence):
			_red_light_reported = false
			_violation_reported = false
			_violation_evidence.clear()
			return
		violation_detected.emit(self, String(_violation_evidence["title"]))
		violation_evidence_detected.emit(self, _violation_evidence.duplicate(true))

func has_reported_violation() -> bool:
	return _violation_reported or _red_light_reported

func get_violation_evidence() -> Dictionary:
	return _violation_evidence.duplicate(true)

func stop_for_police() -> void:
	if is_stopped:
		return
	is_stopped = true
	velocity = Vector3.ZERO
	stopped.emit(self)

func interact() -> void:
	stop_for_police()

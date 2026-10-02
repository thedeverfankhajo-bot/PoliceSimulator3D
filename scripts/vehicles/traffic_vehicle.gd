extends CharacterBody3D
class_name TrafficVehicle

signal violation_detected(vehicle: TrafficVehicle, violation: String)
signal stopped(vehicle: TrafficVehicle)

@export var vehicle_id := "civilian_sedan_001"
@export_range(0.0, 100.0, 1.0) var traffic_speed_kmh: float = 54.0
@export_range(0.0, 100.0, 1.0) var speed_limit_kmh: float = 50.0
@export var loop_start_z: float = 18.0
@export var loop_end_z: float = -24.0

var is_stopped := false
var _violation_reported := false
var _loop_start_position := Vector3.ZERO
var _loop_reset_rotation := Basis.IDENTITY


func _ready() -> void:
	_loop_start_position = global_position
	_loop_reset_rotation = global_transform.basis

func _physics_process(_delta: float) -> void:
	if is_stopped:
		velocity = Vector3.ZERO
		return
	velocity = get_traffic_velocity()
	move_and_slide()
	_check_violation()
	if global_position.z <= loop_end_z:
		_reset_to_loop_start()

func _reset_to_loop_start() -> void:
	var reset_position := _loop_start_position
	reset_position.z = loop_start_z
	global_position = reset_position
	global_transform.basis = _loop_reset_rotation
	_violation_reported = false
	velocity = get_traffic_velocity()

func get_traffic_velocity() -> Vector3:
	return -transform.basis.z * (traffic_speed_kmh / 3.6)

func _check_violation() -> void:
	if _violation_reported or traffic_speed_kmh <= speed_limit_kmh:
		return
	_violation_reported = true
	violation_detected.emit(self, "سرعت غیرمجاز")

func stop_for_police() -> void:
	if is_stopped:
		return
	is_stopped = true
	velocity = Vector3.ZERO
	stopped.emit(self)

func interact() -> void:
	stop_for_police()

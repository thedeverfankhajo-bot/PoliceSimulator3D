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

func _ready() -> void:
	_check_violation()

func _physics_process(_delta: float) -> void:
	if is_stopped:
		velocity = Vector3.ZERO
		return
	velocity = -global_transform.basis.z * (traffic_speed_kmh / 3.6)
	move_and_slide()
	_check_violation()
	if global_position.z <= loop_end_z:
		global_position.z = loop_start_z

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

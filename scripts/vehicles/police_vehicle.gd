extends VehicleBody3D
class_name PoliceVehicle

signal entered(vehicle: PoliceVehicle)

@export var vehicle_id: String = "police_sedan"
@export_range(0.0, 3000.0, 50.0) var max_engine_force: float = 60.0
@export_range(0.0, 1.0, 0.05) var steering_limit: float = 0.35
@export_range(0.0, 2000.0, 50.0) var max_brake_force: float = 40.0
@export_range(0.0, 50.0, 0.5) var max_entry_speed_kmh: float = 5.0

var is_occupied := false

func _physics_process(_delta: float) -> void:
	if not is_occupied:
		engine_force = 0.0
		steering = 0.0
		brake = max_brake_force
		return

	var throttle := Input.get_axis("vehicle_reverse", "vehicle_accelerate")
	var steer_input := Input.get_axis("vehicle_left", "vehicle_right")
	engine_force = throttle * max_engine_force
	steering = steer_input * steering_limit
	brake = max_brake_force if Input.is_action_pressed("vehicle_brake") else 0.0

func interact() -> void:
	if is_occupied:
		return
	if linear_velocity.length() > max_entry_speed_kmh / 3.6:
		return
	entered.emit(self)

func set_occupied(value: bool) -> void:
	is_occupied = value
	if not value:
		engine_force = 0.0
		steering = 0.0
		brake = max_brake_force

func get_exit_position() -> Vector3:
	var exit_point := get_node_or_null("ExitPoint") as Marker3D
	if exit_point != null:
		return exit_point.global_position
	return global_position + global_transform.basis.x * 2.1

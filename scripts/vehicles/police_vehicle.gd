extends VehicleBody3D
class_name PoliceVehicle

signal entered(vehicle: PoliceVehicle)

@export var vehicle_id: String = "police_sedan"
@export_range(0.0, 3000.0, 50.0) var max_engine_force: float = 900.0
@export_range(0.0, 1.0, 0.05) var steering_limit: float = 0.35
@export_range(0.0, 2000.0, 50.0) var max_brake_force: float = 700.0

func _physics_process(_delta: float) -> void:
	var throttle := Input.get_axis("vehicle_reverse", "vehicle_accelerate")
	var steer_input := Input.get_axis("vehicle_left", "vehicle_right")
	engine_force = throttle * max_engine_force
	steering = steer_input * steering_limit
	brake = max_brake_force if Input.is_action_pressed("vehicle_brake") else 0.0

func interact() -> void:
	entered.emit(self)

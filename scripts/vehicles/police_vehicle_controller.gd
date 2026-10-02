extends VehicleBody3D
class_name PoliceVehicleController

@export_range(0.0, 3000.0, 50.0) var engine_force := 900.0
@export_range(0.0, 1.0, 0.05) var steering_limit := 0.35
@export_range(0.0, 2000.0, 50.0) var brake_force := 700.0

func _physics_process(_delta: float) -> void:
	var throttle := Input.get_axis("move_backward", "move_forward")
	var steering := Input.get_axis("move_left", "move_right")
	engine_force = throttle * 900.0
	steering = steering * steering_limit
	self.steering = steering
	brake = brake_force if Input.is_action_pressed("interact") else 0.0

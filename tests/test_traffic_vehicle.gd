extends SceneTree

const TRAFFIC_VEHICLE_SCRIPT := preload("res://scripts/vehicles/traffic_vehicle.gd")

func _initialize() -> void:
	var vehicle = TRAFFIC_VEHICLE_SCRIPT.new()
	var violation_count := 0
	vehicle.violation_detected.connect(func(_vehicle, _violation: String) -> void:
		violation_count += 1
	)
	assert(vehicle.get_traffic_velocity().length() > 0.0, "Traffic vehicle must have forward velocity.")
	vehicle._check_violation()
	assert(violation_count == 1, "Speeding vehicle must report exactly one violation.")
	vehicle._check_violation()
	assert(violation_count == 1, "The same violation must not be reported twice.")
	vehicle.stop_for_police()
	assert(vehicle.is_stopped, "Traffic vehicle must stop for police.")
	vehicle.stop_for_police()
	assert(vehicle.is_stopped, "Stopping twice must remain idempotent.")
	print("Traffic vehicle tests passed.")
	vehicle.free()
	quit(0)

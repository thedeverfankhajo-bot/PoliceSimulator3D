extends SceneTree

const TRAFFIC_VEHICLE_SCRIPT := preload("res://scripts/vehicles/traffic_vehicle.gd")

func _fail(message: String) -> void:
	push_error(message)
	quit(1)

func _initialize() -> void:
	var vehicle = TRAFFIC_VEHICLE_SCRIPT.new()
	if vehicle.get_traffic_velocity().length() <= 0.0:
		_fail("Traffic vehicle must have forward velocity.")
		return
	vehicle._check_violation()
	if not vehicle._violation_reported:
		_fail("Speeding vehicle must record a violation.")
		return
	vehicle._check_violation()
	if not vehicle._violation_reported:
		_fail("Repeated violation check must keep the violation recorded.")
		return
	vehicle.stop_for_police()
	if not vehicle.is_stopped:
		_fail("Traffic vehicle must stop for police.")
		return
	vehicle.stop_for_police()
	if not vehicle.is_stopped:
		_fail("Stopping twice must remain idempotent.")
		return
	vehicle.free()
	print("Traffic vehicle tests passed.")
	quit(0)

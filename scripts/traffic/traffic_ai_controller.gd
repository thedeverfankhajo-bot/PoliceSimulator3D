extends Node
class_name TrafficAIController

@export_range(1.0, 30.0, 0.5) var safe_follow_distance := 7.0
@export_range(0.5, 8.0, 0.5) var full_stop_distance := 2.5
@export_range(0.1, 4.0, 0.1) var lane_width := 2.5
@export_range(0.1, 1.0, 0.05) var minimum_speed_factor := 0.2

var _vehicles: Array[Node3D] = []

func register_vehicle(vehicle: Node3D) -> void:
	if vehicle == null or not is_instance_valid(vehicle):
		return
	if not _vehicles.has(vehicle):
		_vehicles.append(vehicle)

func unregister_vehicle(vehicle: Node3D) -> void:
	_vehicles.erase(vehicle)

func _physics_process(_delta: float) -> void:
	_cleanup()
	for vehicle in _vehicles:
		if not is_instance_valid(vehicle) or not vehicle.has_method("set_ai_speed_factor"):
			continue
		var desired_factor := 1.0
		var stop_requested := false
		var leader := _find_leader(vehicle)
		if leader != null:
			var gap := vehicle.global_position.z - leader.global_position.z
			if gap <= full_stop_distance:
				stop_requested = true
			elif gap < safe_follow_distance:
				desired_factor = clamp(gap / safe_follow_distance, minimum_speed_factor, 1.0)
		vehicle.set_ai_speed_factor(desired_factor)
		if vehicle.has_method("set_ai_stop_requested"):
			vehicle.set_ai_stop_requested(stop_requested)

func _find_leader(vehicle: Node3D) -> Node3D:
	var best: Node3D
	var best_gap := INF
	for candidate in _vehicles:
		if candidate == vehicle or not is_instance_valid(candidate):
			continue
		if abs(candidate.global_position.x - vehicle.global_position.x) > lane_width:
			continue
		var gap := vehicle.global_position.z - candidate.global_position.z
		if gap > 0.0 and gap < best_gap:
			best_gap = gap
			best = candidate
	return best

func _cleanup() -> void:
	for i in range(_vehicles.size() - 1, -1, -1):
		if not is_instance_valid(_vehicles[i]):
			_vehicles.remove_at(i)

func get_registered_vehicle_count() -> int:
	_cleanup()
	return _vehicles.size()

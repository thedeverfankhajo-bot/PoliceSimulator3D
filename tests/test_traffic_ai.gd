extends SceneTree

const AI_SCRIPT := preload("res://scripts/traffic/traffic_ai_controller.gd")

func _fail(message: String) -> void:
	push_error(message)
	quit(1)

func _initialize() -> void:
	var controller = AI_SCRIPT.new()
	var root := Node.new()
	add_child(root)
	var leader := Node3D.new()
	leader.position = Vector3(0, 0, 0)
	var follower := Node3D.new()
	follower.position = Vector3(0, 0, 5)
	root.add_child(leader)
	root.add_child(follower)
	controller.register_vehicle(leader)
	controller.register_vehicle(follower)
	if controller.get_registered_vehicle_count() != 2:
		_fail("Traffic AI must register unique vehicles.")
		return
	controller.unregister_vehicle(leader)
	if controller.get_registered_vehicle_count() != 1:
		_fail("Traffic AI must unregister vehicles.")
		return
	follower.queue_free()
	leader.queue_free()
	root.free()
	controller.free()
	print("Traffic AI tests passed.")
	quit(0)

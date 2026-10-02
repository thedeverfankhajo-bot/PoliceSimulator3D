extends Node3D
class_name InteractionDetector

@export_range(0.5, 10.0, 0.1) var interaction_distance: float = 3.0
@export_flags_3d_physics var collision_mask: int = 1
@onready var camera: Camera3D = get_parent().get_node_or_null("Camera3D") as Camera3D

signal interacted(target: Node)
signal interaction_missed()

func _physics_process(_delta: float) -> void:
	if Input.is_action_just_pressed("interact"):
		# Desktop requires a captured mouse for the interaction key, while
		# touch devices drive the same action through the mobile UI.
		var mobile_input := OS.has_feature("mobile") or DisplayServer.is_touchscreen_available()
		if mobile_input or Input.mouse_mode == Input.MOUSE_MODE_CAPTURED:
			try_interact()

func try_interact() -> void:
	if not is_inside_tree() or camera == null:
		interaction_missed.emit()
		return

	var from := camera.global_position
	var to := from + (-camera.global_transform.basis.z * interaction_distance)
	var query := PhysicsRayQueryParameters3D.create(from, to, collision_mask)
	query.collide_with_areas = true
	query.collide_with_bodies = true
	query.exclude = [get_parent().get_parent().get_rid()]
	var result := get_world_3d().direct_space_state.intersect_ray(query)
	if result.is_empty():
		interaction_missed.emit()
		return

	var collider: Variant = result.get("collider")
	if collider is Node:
		var target := _find_interactable(collider as Node)
		if target != null:
			target.interact()
			interacted.emit(target)
			return
	interaction_missed.emit()

func _find_interactable(start: Node) -> Node:
	var current: Node = start
	while current != null:
		if current.has_method("interact"):
			return current
		current = current.get_parent()
	return null

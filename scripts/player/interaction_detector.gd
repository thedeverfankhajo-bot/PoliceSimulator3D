extends Node3D
class_name InteractionDetector

@export var interaction_distance: float = 3.0
@export_flags_3d_physics var collision_mask: int = 1
@onready var camera: Camera3D = $Camera3D

signal interacted(target: Node)
signal interaction_missed()

func _physics_process(_delta: float) -> void:
	if Input.is_action_just_pressed("interact"):
		try_interact()

func try_interact() -> void:
	if camera == null:
		interaction_missed.emit()
		return

	var from := camera.global_position
	var to := from + (-camera.global_transform.basis.z * interaction_distance)
	var query := PhysicsRayQueryParameters3D.create(from, to, collision_mask)
	query.collide_with_areas = true
	query.collide_with_bodies = true
	var result := get_world_3d().direct_space_state.intersect_ray(query)
	if result.is_empty():
		interaction_missed.emit()
		return

	var target := result.get("collider") as Node
	if target != null and target.has_method("interact"):
		target.interact()
		interacted.emit(target)
	else:
		interaction_missed.emit()

extends Node3D
class_name InteractionDetector

@export var interaction_distance: float = 3.0
@export var collision_mask: int = 1
@onready var camera: Camera3D = $Camera3D

signal interactable_found(target: Node)
signal interaction_failed()

func try_interact() -> void:
	var from := camera.global_position
	var to := from + (-camera.global_transform.basis.z * interaction_distance)
	var query := PhysicsRayQueryParameters3D.create(from, to, collision_mask)
	query.collide_with_areas = true
	query.collide_with_bodies = true
	var result := get_world_3d().direct_space_state.intersect_ray(query)
	if result.is_empty():
		interaction_failed.emit()
		return
	var target: Node = result["collider"]
	if target.has_method("interact"):
		target.interact()
		interactable_found.emit(target)
	else:
		interaction_failed.emit()

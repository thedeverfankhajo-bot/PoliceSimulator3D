extends StaticBody3D
class_name CivilianNPC

signal interacted(npc)

@export var npc_id := "civilian_001"

func interact() -> void:
	interacted.emit(self)

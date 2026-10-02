extends Node
class_name GameSession

var mission_manager := MissionManager.new()

func _ready() -> void:
	add_child(mission_manager)

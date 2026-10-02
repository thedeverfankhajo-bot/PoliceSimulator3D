extends Node
class_name GameSession

const MISSION_MANAGER_SCRIPT := preload("res://scripts/missions/mission_manager.gd")

var mission_manager = MISSION_MANAGER_SCRIPT.new()

func _ready() -> void:
	add_child(mission_manager)

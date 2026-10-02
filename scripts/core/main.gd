extends Node3D
## Main world composition. Gameplay rules belong in isolated systems.

@onready var player_spawn: Marker3D = $PlayerSpawn

func _ready() -> void:
	var player_scene := preload("res://scenes/player/player.tscn")
	var player := player_scene.instantiate()
	player.global_position = player_spawn.global_position
	add_child(player)

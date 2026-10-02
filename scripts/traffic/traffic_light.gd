extends Node3D
class_name TrafficLight

signal state_changed(is_red: bool)

@export var light_id := "intersection_001"
@export var stop_line_z := -3.0
@export var cycle_green_seconds := 8.0
@export var cycle_red_seconds := 6.0

var _is_red := false
var _timer := 0.0

func _ready() -> void:
	_timer = cycle_green_seconds

func _process(delta: float) -> void:
	_timer -= delta
	if _timer > 0.0:
		return
	_is_red = not _is_red
	_timer = cycle_red_seconds if _is_red else cycle_green_seconds
	state_changed.emit(_is_red)

func is_red() -> bool:
	return _is_red

func get_stop_line_z() -> float:
	return stop_line_z

func get_light_id() -> String:
	return light_id

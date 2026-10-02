extends Node

signal changed

const PATH := "user://settings.cfg"
const DEFAULTS := {
	"audio/master_db": 0.0,
	"audio/music_db": -6.0,
	"audio/sfx_db": -3.0,
	"display/brightness": 1.0,
	"display/ui_scale": 1.0,
	"display/fps_limit": 60,
	"gameplay/mouse_sensitivity": 0.0025,
	"gameplay/invert_y": false,
	"gameplay/show_hints": true,
	"gameplay/auto_save": true,
	"accessibility/high_contrast": false
}

var values: Dictionary = DEFAULTS.duplicate(true)

func _ready() -> void:
	load_settings()

func load_settings() -> void:
	var cfg := ConfigFile.new()
	if cfg.load(PATH) != OK:
		values = DEFAULTS.duplicate(true)
		return
	for key in DEFAULTS:
		var parts := String(key).split("/", false, 1)
		values[key] = cfg.get_value(parts[0], parts[1], DEFAULTS[key])

func set_value(key: String, value) -> void:
	if not DEFAULTS.has(key):
		return
	values[key] = value
	save_settings()
	changed.emit()

func get_value(key: String):
	return values.get(key, DEFAULTS.get(key))

func save_settings() -> Error:
	var cfg := ConfigFile.new()
	for key in values:
		var parts := String(key).split("/", false, 1)
		cfg.set_value(parts[0], parts[1], values[key])
	return cfg.save(PATH)

func reset() -> void:
	values = DEFAULTS.duplicate(true)
	save_settings()
	changed.emit()

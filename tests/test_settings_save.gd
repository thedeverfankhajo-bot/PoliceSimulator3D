extends SceneTree

const SETTINGS := preload("res://scripts/core/game_settings.gd")
const SAVE := preload("res://scripts/core/save_manager.gd")

func _fail(message: String) -> void:
	push_error(message)
	quit(1)

func _initialize() -> void:
	var settings = SETTINGS.new()
	settings.set_value("gameplay/auto_save", false)
	if settings.get_value("gameplay/auto_save") != false:
		_fail("Settings value was not persisted in memory.")
		return
	var save = SAVE.new()
	var player := Node3D.new()
	player.global_position = Vector3(4, 1, -8)
	if save.save_game(player, null, true) != OK:
		_fail("Save failed.")
		return
	if not save.has_save():
		_fail("Save file was not created.")
		return
	var loaded = SAVE.new()
	if not loaded.load_game():
		_fail("Save file could not be loaded.")
		return
	if loaded.data["mission_completed"] != true:
		_fail("Saved mission state was not restored.")
		return
	if loaded.data["last_position"] != Vector3(4, 1, -8):
		_fail("Saved player position was not restored.")
		return
	save.delete_save()
	loaded.delete_save()
	settings.reset()
	settings.free()
	save.free()
	loaded.free()
	player.free()
	print("Settings and save tests passed.")
	quit(0)

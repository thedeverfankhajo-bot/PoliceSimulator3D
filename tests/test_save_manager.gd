extends SceneTree

const SAVE_SCRIPT := preload("res://scripts/core/save_manager.gd")

func _initialize() -> void:
	var save_manager := SAVE_SCRIPT.new()
	assert(save_manager.load_game() == false)

	var cfg := ConfigFile.new()
	cfg.set_value("save", "version", 1)
	cfg.set_value("save", "xp", -1)
	cfg.set_value("save", "rank", "کارآموز")
	cfg.set_value("save", "mission_completed", false)
	cfg.set_value("save", "position_x", 0.0)
	cfg.set_value("save", "position_y", 0.9)
	cfg.set_value("save", "position_z", 8.0)
	assert(cfg.save("user://savegame.cfg") == OK)
	assert(save_manager.load_game() == false)

	cfg.set_value("save", "xp", 10)
	cfg.set_value("save", "position_x", 999999.0)
	assert(cfg.save("user://savegame.cfg") == OK)
	assert(save_manager.load_game() == false)

	cfg.set_value("save", "position_x", 0.0)
	cfg.set_value("save", "position_y", 0.9)
	cfg.set_value("save", "position_z", 8.0)
	assert(cfg.save("user://savegame.cfg") == OK)
	assert(save_manager.load_game() == true)
	assert(save_manager.data["xp"] == 10)
	assert(save_manager.data["last_position"] == Vector3(0.0, 0.9, 8.0))
	assert(save_manager.delete_save() == OK)

	print("Save manager validation tests passed.")
	quit(0)

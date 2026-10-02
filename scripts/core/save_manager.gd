extends Node

signal saved
signal loaded

const PATH := "user://savegame.cfg"
const VERSION := 1

var data: Dictionary = {
	"version": VERSION,
	"xp": 0,
	"rank": "کارآموز",
	"mission_completed": false,
	"last_position": Vector3(0, 0.9, 8)
}

func save_game(player: Node = null, career = null, mission_completed := false) -> Error:
	if is_instance_valid(player):
		data["last_position"] = player.global_position
	if career != null:
		data["xp"] = career.xp
		data["rank"] = career.get_rank()
	data["mission_completed"] = mission_completed
	data["version"] = VERSION
	var cfg := ConfigFile.new()
	cfg.set_value("save", "version", VERSION)
	cfg.set_value("save", "xp", int(data["xp"]))
	cfg.set_value("save", "rank", String(data["rank"]))
	cfg.set_value("save", "mission_completed", bool(data["mission_completed"]))
	var p: Vector3 = data["last_position"]
	cfg.set_value("save", "position_x", p.x)
	cfg.set_value("save", "position_y", p.y)
	cfg.set_value("save", "position_z", p.z)
	var err := cfg.save(PATH)
	if err == OK:
		saved.emit()
	return err

func load_game() -> bool:
	var cfg := ConfigFile.new()
	if cfg.load(PATH) != OK:
		return false
	var version := int(cfg.get_value("save", "version", 0))
	if version != VERSION:
		return false
	data["xp"] = int(cfg.get_value("save", "xp", 0))
	data["rank"] = String(cfg.get_value("save", "rank", "کارآموز"))
	data["mission_completed"] = bool(cfg.get_value("save", "mission_completed", false))
	data["last_position"] = Vector3(
		float(cfg.get_value("save", "position_x", 0.0)),
		float(cfg.get_value("save", "position_y", 0.9)),
		float(cfg.get_value("save", "position_z", 8.0))
	)
	loaded.emit()
	return true

func has_save() -> bool:
	return FileAccess.file_exists(PATH)

func delete_save() -> Error:
	if not has_save():
		return OK
	return DirAccess.remove_absolute(ProjectSettings.globalize_path(PATH))

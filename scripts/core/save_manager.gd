extends Node

signal saved
signal loaded

const PATH: String = "user://savegame.cfg"
const VERSION: int = 1
const DEFAULT_POSITION: Vector3 = Vector3(0, 0.9, 8)

var data: Dictionary[String, Variant] = {
	"version": VERSION,
	"xp": 0,
	"rank": "کارآموز",
	"mission_completed": false,
	"last_position": DEFAULT_POSITION
}

func save_game(player: Node = null, career: Object = null, mission_completed: bool = false) -> Error:
	if is_instance_valid(player) and player is Node3D:
		data["last_position"] = (player as Node3D).global_position
	if career != null:
		data["xp"] = career.xp
		data["rank"] = career.get_rank()
	data["mission_completed"] = mission_completed
	data["version"] = VERSION
	var cfg: ConfigFile = ConfigFile.new()
	cfg.set_value("save", "version", VERSION)
	cfg.set_value("save", "xp", int(data["xp"]))
	cfg.set_value("save", "rank", String(data["rank"]))
	cfg.set_value("save", "mission_completed", bool(data["mission_completed"]))
	var position: Vector3 = data["last_position"] as Vector3
	cfg.set_value("save", "position_x", position.x)
	cfg.set_value("save", "position_y", position.y)
	cfg.set_value("save", "position_z", position.z)
	var err: Error = cfg.save(PATH)
	if err == OK:
		saved.emit()
	return err

func load_game() -> bool:
	var cfg: ConfigFile = ConfigFile.new()
	if cfg.load(PATH) != OK:
		return false
	var version: int = int(cfg.get_value("save", "version", 0))
	if version != VERSION:
		return false
	data["xp"] = int(cfg.get_value("save", "xp", 0))
	data["rank"] = String(cfg.get_value("save", "rank", "کارآموز"))
	data["mission_completed"] = bool(cfg.get_value("save", "mission_completed", false))
	data["last_position"] = Vector3(
		float(cfg.get_value("save", "position_x", DEFAULT_POSITION.x)),
		float(cfg.get_value("save", "position_y", DEFAULT_POSITION.y)),
		float(cfg.get_value("save", "position_z", DEFAULT_POSITION.z))
	)
	loaded.emit()
	return true

func has_save() -> bool:
	return FileAccess.file_exists(PATH)

func delete_save() -> Error:
	if not has_save():
		return OK
	return DirAccess.remove_absolute(ProjectSettings.globalize_path(PATH))

extends CanvasLayer
class_name StatusHud

@onready var status_label: Label = $Status

var _mission

func set_status(message: String) -> void:
	status_label.text = message

func show_mission(mission) -> void:
	if _mission != null and is_instance_valid(_mission):
		if _mission.objective_completed.is_connected(_on_objective_completed):
			_mission.objective_completed.disconnect(_on_objective_completed)
		if _mission.status_changed.is_connected(_on_mission_status_changed):
			_mission.status_changed.disconnect(_on_mission_status_changed)
	_mission = mission
	if _mission == null:
		set_status("هیچ مأموریتی فعال نیست.")
		return
	if not _mission.objective_completed.is_connected(_on_objective_completed):
		_mission.objective_completed.connect(_on_objective_completed)
	if not _mission.status_changed.is_connected(_on_mission_status_changed):
		_mission.status_changed.connect(_on_mission_status_changed)
	_render_mission()

func _on_mission_status_changed(_status: int) -> void:
	_render_mission()

func _on_objective_completed(_index: int) -> void:
	_render_mission()

func _render_mission() -> void:
	if _mission == null:
		return
	var lines: Array[String] = [
		_mission.title,
		_mission.description
	]
	var objectives: Array[Dictionary] = _mission.get_objectives()
	for i in objectives.size():
		var objective_title := String(objectives[i].get("title", "هدف نامشخص"))
		var marker := "[x]" if _mission.is_objective_completed(i) else "[ ]"
		lines.append("%s %s" % [marker, objective_title])
	set_status("\n".join(lines))

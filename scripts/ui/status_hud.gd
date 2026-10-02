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
	_mission = mission
	if _mission == null:
		set_status("هیچ مأموریتی فعال نیست.")
		return
	_mission.objective_completed.connect(_on_objective_completed)
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
	var objectives := _mission.get_objectives()
	for i in objectives.size():
		var objective_title := String(objectives[i].get("title", "هدف نامشخص"))
		var marker := "[x]" if _mission.is_objective_completed(i) else "[ ]"
		lines.append("%s %s" % [marker, objective_title])
	set_status("\n".join(lines))

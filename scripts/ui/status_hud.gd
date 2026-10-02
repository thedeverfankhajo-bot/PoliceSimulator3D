extends CanvasLayer
class_name StatusHud

@onready var status_label: Label = $Status

func set_status(message: String) -> void:
	status_label.text = message

func show_mission(mission: Mission) -> void:
	set_status("%s\n%s" % [mission.title, mission.description])

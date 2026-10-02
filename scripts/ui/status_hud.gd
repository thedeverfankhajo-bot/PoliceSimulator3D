extends CanvasLayer
class_name StatusHud

@onready var status_label: Label = $Status

func set_status(message: String) -> void:
	status_label.text = message

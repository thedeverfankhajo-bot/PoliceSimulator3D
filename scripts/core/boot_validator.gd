extends Node
## Minimal runtime bootstrap validation.
## This intentionally contains no gameplay logic.

const PROJECT_NAME := "PoliceSimulator3D"

func _ready() -> void:
	assert(ProjectSettings.get_setting("application/config/name", "") == PROJECT_NAME)
	print("[BOOT] %s bootstrap OK" % PROJECT_NAME)

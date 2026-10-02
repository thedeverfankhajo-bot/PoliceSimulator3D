extends SceneTree

const CITATION_SCRIPT := preload("res://scripts/core/citation_manager.gd")

func _fail(message: String) -> void:
	push_error(message)
	quit(1)

func _initialize() -> void:
	var manager = CITATION_SCRIPT.new()
	var invalid := manager.issue_from_evidence({})
	if not invalid.is_empty():
		_fail("Invalid evidence must not create a citation.")
		return
	var citation := manager.issue_from_evidence({
		"type": "speeding",
		"title": "سرعت غیرمجاز",
		"observed_speed_kmh": 68.0,
		"speed_limit_kmh": 50.0
	}, "warning")
	if citation.is_empty():
		_fail("Valid evidence must create a citation.")
		return
	if int(citation["number"]) != 1:
		_fail("First citation number must be 1.")
		return
	if String(citation["action"]) != "warning":
		_fail("Citation action must be preserved.")
		return
	if manager.get_issued_count() != 1:
		_fail("Citation history must contain the issued citation.")
		return
	var history := manager.get_history()
	history[0]["action"] = "tampered"
	if String(manager.get_history()[0]["action"]) != "warning":
		_fail("Citation history must be returned as a deep copy.")
		return
	manager.free()
	print("Citation manager tests passed.")
	quit(0)

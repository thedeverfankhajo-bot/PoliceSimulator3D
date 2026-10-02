extends SceneTree

const VIOLATION_SCRIPT := preload("res://scripts/violations/traffic_violation.gd")
const LIGHT_SCRIPT := preload("res://scripts/traffic/traffic_light.gd")

func _fail(message: String) -> void:
	push_error(message)
	quit(1)

func _initialize() -> void:
	var evidence := VIOLATION_SCRIPT.create_red_light_evidence("intersection_001", -3.0)
	if String(evidence.get("id", "")) != "red_light":
		_fail("Red-light evidence must expose a stable violation id.")
		return
	if not VIOLATION_SCRIPT.is_valid_evidence(evidence):
		_fail("Generated red-light evidence must validate.")
		return
	var invalid := evidence.duplicate(true)
	invalid["traffic_light_id"] = ""
	if VIOLATION_SCRIPT.is_valid_evidence(invalid):
		_fail("Red-light evidence without a traffic light id must be rejected.")
		return

	var light = LIGHT_SCRIPT.new()
	if light.is_red():
		_fail("Traffic light must start green.")
		return
	light._timer = 0.0
	light._process(0.01)
	if not light.is_red():
		_fail("Traffic light must transition to red.")
		return
	if light.get_light_id() != "intersection_001":
		_fail("Traffic light id must remain stable.")
		return
	light.free()
	print("Traffic light tests passed.")
	quit(0)

extends SceneTree

const VIOLATION_SCRIPT := preload("res://scripts/violations/traffic_violation.gd")

func _fail(message: String) -> void:
	push_error(message)
	quit(1)

func _initialize() -> void:
	var evidence := VIOLATION_SCRIPT.create_speeding_evidence(68.0, 50.0)
	if String(evidence["id"]) != "speeding":
		_fail("Speeding evidence must expose a stable violation id.")
		return
	if String(evidence["title"]) != "سرعت غیرمجاز":
		_fail("Speeding evidence must expose the expected title.")
		return
	if not is_equal_approx(float(evidence["observed_speed_kmh"]), 68.0):
		_fail("Observed speed must be preserved.")
		return
	if not is_equal_approx(float(evidence["speed_limit_kmh"]), 50.0):
		_fail("Speed limit must be preserved.")
		return
	if not is_equal_approx(float(evidence["excess_speed_kmh"]), 18.0):
		_fail("Excess speed must be calculated.")
		return
	if not VIOLATION_SCRIPT.is_valid_evidence(evidence):
		_fail("Generated violation evidence must validate.")
		return

	var invalid := evidence.duplicate(true)
	invalid["excess_speed_kmh"] = -1.0
	if VIOLATION_SCRIPT.is_valid_evidence(invalid):
		_fail("Negative excess speed must be rejected.")
		return

	print("Traffic violation tests passed.")
	quit(0)

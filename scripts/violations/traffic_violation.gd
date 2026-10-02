extends RefCounted
class_name TrafficViolation

const SPEEDING_ID := "speeding"
const SPEEDING_TITLE := "سرعت غیرمجاز"

static func create_speeding_evidence(observed_speed_kmh: float, speed_limit_kmh: float) -> Dictionary:
	var excess_speed_kmh := maxf(0.0, observed_speed_kmh - speed_limit_kmh)
	return {
		"id": SPEEDING_ID,
		"title": SPEEDING_TITLE,
		"observed_speed_kmh": observed_speed_kmh,
		"speed_limit_kmh": speed_limit_kmh,
		"excess_speed_kmh": excess_speed_kmh
	}

static func is_valid_evidence(evidence: Dictionary) -> bool:
	var observed_speed := float(evidence.get("observed_speed_kmh", -1.0))
	var speed_limit := float(evidence.get("speed_limit_kmh", -1.0))
	var excess_speed := float(evidence.get("excess_speed_kmh", -1.0))
	if String(evidence.get("id", "")).strip_edges() != SPEEDING_ID:
		return false
	if String(evidence.get("title", "")).strip_edges() != SPEEDING_TITLE:
		return false
	if observed_speed < 0.0 or speed_limit < 0.0 or excess_speed < 0.0:
		return false
	return is_equal_approx(excess_speed, maxf(0.0, observed_speed - speed_limit))


static func create_red_light_evidence(light_id: String, stop_line_z: float) -> Dictionary:
	return {
		"id": "red_light",
		"title": "عبور از چراغ قرمز",
		"traffic_light_id": light_id,
		"stop_line_z": stop_line_z
	}

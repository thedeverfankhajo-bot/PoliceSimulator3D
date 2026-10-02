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
	return (
		String(evidence.get("id", "")).strip_edges() == SPEEDING_ID
		and String(evidence.get("title", "")).strip_edges() == SPEEDING_TITLE
		and float(evidence.get("observed_speed_kmh", -1.0)) >= 0.0
		and float(evidence.get("speed_limit_kmh", -1.0)) >= 0.0
		and float(evidence.get("excess_speed_kmh", -1.0)) >= 0.0
	)

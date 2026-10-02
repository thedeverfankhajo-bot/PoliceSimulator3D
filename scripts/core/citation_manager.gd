extends Node
class_name CitationManager

signal citation_issued(citation: Dictionary)

const MAX_HISTORY := 100

var _history: Array[Dictionary] = []
var _next_number := 1

func issue_from_evidence(evidence: Dictionary, action: String = "warning") -> Dictionary:
	if not _is_valid_evidence(evidence):
		return {}
	var citation := {
		"number": _next_number,
		"action": action,
		"title": String(evidence.get("title", "Traffic violation")),
		"violation_type": String(evidence.get("type", "unknown")),
		"observed_speed_kmh": float(evidence.get("observed_speed_kmh", 0.0)),
		"speed_limit_kmh": float(evidence.get("speed_limit_kmh", 0.0)),
		"issued_at_msec": Time.get_ticks_msec()
	}
	_next_number += 1
	_history.append(citation)
	if _history.size() > MAX_HISTORY:
		_history.pop_front()
	citation_issued.emit(citation.duplicate(true))
	return citation.duplicate(true)

func get_history() -> Array[Dictionary]:
	return _history.duplicate(true)

func get_issued_count() -> int:
	return _history.size()

func _is_valid_evidence(evidence: Dictionary) -> bool:
	if evidence.is_empty():
		return false
	if not evidence.has("type") or not evidence.has("title"):
		return false
	return not String(evidence["type"]).is_empty() and not String(evidence["title"]).is_empty()

extends RefCounted
class_name Mission

const MISSION_STATE := preload("res://scripts/missions/mission_state.gd")

signal status_changed(status: int)
signal objective_completed(index: int)
signal completed
signal failed(reason: String)

@export var mission_id := ""
@export var title := ""
@export_multiline var description := ""

var status: int = MISSION_STATE.Status.AVAILABLE
var failure_reason := ""
var _objectives: Array[Dictionary] = []
var _completed: Array[bool] = []

func configure(objectives: Array[Dictionary]) -> bool:
	if status == MISSION_STATE.Status.ACTIVE or status == MISSION_STATE.Status.COMPLETED:
		return false
	if objectives.is_empty():
		return false
	var ids := {}
	for objective in objectives:
		if not objective.has("id") or not objective.has("title"):
			return false
		var id := String(objective["id"]).strip_edges()
		var objective_title := String(objective["title"]).strip_edges()
		if id.is_empty() or objective_title.is_empty() or ids.has(id):
			return false
		ids[id] = true
	_objectives = objectives.duplicate(true)
	_completed.clear()
	_completed.resize(_objectives.size())
	for i in _completed.size():
		_completed[i] = false
	failure_reason = ""
	return true

func get_objectives() -> Array[Dictionary]:
	return _objectives.duplicate(true)

func is_objective_completed(index: int) -> bool:
	if index < 0 or index >= _completed.size():
		return false
	return _completed[index]

func start() -> bool:
	if status != MISSION_STATE.Status.AVAILABLE or _objectives.is_empty():
		return false
	status = MISSION_STATE.Status.ACTIVE
	status_changed.emit(status)
	return true

func complete_objective(index: int) -> bool:
	if status != MISSION_STATE.Status.ACTIVE:
		return false
	if index < 0 or index >= _completed.size() or _completed[index]:
		return false
	_completed[index] = true
	objective_completed.emit(index)
	if _completed.all(func(done: bool) -> bool: return done):
		status = MISSION_STATE.Status.COMPLETED
		status_changed.emit(status)
		completed.emit()
	return true

func fail(reason: String) -> bool:
	if status != MISSION_STATE.Status.ACTIVE:
		return false
	failure_reason = reason.strip_edges()
	status = MISSION_STATE.Status.FAILED
	status_changed.emit(status)
	failed.emit(failure_reason)
	return true

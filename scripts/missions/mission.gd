extends Node
class_name Mission

signal status_changed(status: MissionState.Status)
signal objective_completed(index: int)
signal completed

@export var mission_id := ""
@export var title := ""
@export_multiline var description := ""

var status: MissionState.Status = MissionState.Status.AVAILABLE
var _objectives: Array[Dictionary] = []
var _completed: Array[bool] = []

func configure(objectives: Array[Dictionary]) -> void:
	if status == MissionState.Status.ACTIVE:
		return
	_objectives = objectives.duplicate(true)
	_completed.clear()
	_completed.resize(_objectives.size())
	for i in _completed.size():
		_completed[i] = false

func start() -> bool:
	if status != MissionState.Status.AVAILABLE or _objectives.is_empty():
		return false
	status = MissionState.Status.ACTIVE
	status_changed.emit(status)
	return true

func complete_objective(index: int) -> bool:
	if status != MissionState.Status.ACTIVE:
		return false
	if index < 0 or index >= _completed.size() or _completed[index]:
		return false
	_completed[index] = true
	objective_completed.emit(index)
	if _completed.all(func(done: bool) -> bool: return done):
		status = MissionState.Status.COMPLETED
		status_changed.emit(status)
		completed.emit()
	return true

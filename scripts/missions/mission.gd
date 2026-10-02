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
	_objectives = objectives.duplicate(true)
	_completed.clear()
	_completed.resize(_objectives.size())
	for i in _completed.size():
		_completed[i] = false

func start() -> void:
	if status != MissionState.Status.AVAILABLE:
		return
	status = MissionState.Status.ACTIVE
	status_changed.emit(status)

func complete_objective(index: int) -> void:
	if status != MissionState.Status.ACTIVE:
		return
	if index < 0 or index >= _completed.size() or _completed[index]:
		return
	_completed[index] = true
	objective_completed.emit(index)
	if _completed.all(func(done: bool) -> bool: return done):
		status = MissionState.Status.COMPLETED
		status_changed.emit(status)
		completed.emit()

extends Control
class_name MobileControls

## Lightweight touch controls for Android/iOS.
## Touch input is converted to the existing InputMap actions so gameplay code
## stays platform-agnostic. Desktop input remains unchanged.

@export var desktop_preview := false
@export var joystick_radius := 96.0
@export var joystick_deadzone := 18.0
@export var action_radius := 58.0

var _joystick_id := -1
var _joystick_origin := Vector2.ZERO
var _joystick_vector := Vector2.ZERO
var _action_id := -1
var _action_center := Vector2.ZERO
var _visible_on_device := false

func _ready() -> void:
	set_process_input(true)
	_visible_on_device = OS.has_feature("mobile") or DisplayServer.is_touchscreen_available()
	visible = desktop_preview or _visible_on_device
	queue_redraw()

func _notification(what: int) -> void:
	if what == NOTIFICATION_RESIZED:
		queue_redraw()

func _unhandled_input(event: InputEvent) -> void:
	if not visible:
		return
	if event is InputEventScreenTouch:
		_handle_touch(event)
	elif event is InputEventScreenDrag:
		_handle_drag(event)

func _handle_touch(event: InputEventScreenTouch) -> void:
	if event.canceled:
		if event.index == _joystick_id:
			_release_movement()
		if event.index == _action_id:
			_release_action()
		return

	if event.pressed:
		if _joystick_id == -1 and _is_joystick_area(event.position):
			_joystick_id = event.index
			_joystick_origin = event.position
			_update_joystick(event.position)
		elif _action_id == -1 and event.position.distance_to(_action_center) <= action_radius * 1.25:
			_action_id = event.index
			Input.action_press("interact")
	else:
		if event.index == _joystick_id:
			_release_movement()
		elif event.index == _action_id:
			_release_action()

func _handle_drag(event: InputEventScreenDrag) -> void:
	if event.index == _joystick_id:
		_update_joystick(event.position)

func _is_joystick_area(position: Vector2) -> bool:
	return position.x <= size.x * 0.48 and position.y >= size.y * 0.48

func _update_joystick(position: Vector2) -> void:
	var offset := position - _joystick_origin
	if offset.length() > joystick_radius:
		offset = offset.normalized() * joystick_radius
	var value := offset / joystick_radius
	_joystick_vector = value if value.length() >= joystick_deadzone / joystick_radius else Vector2.ZERO
	Input.action_press("move_left", maxf(0.0, -_joystick_vector.x))
	Input.action_press("move_right", maxf(0.0, _joystick_vector.x))
	Input.action_press("move_forward", maxf(0.0, -_joystick_vector.y))
	Input.action_press("move_backward", maxf(0.0, _joystick_vector.y))
	queue_redraw()

func _release_movement() -> void:
	_joystick_id = -1
	_joystick_vector = Vector2.ZERO
	Input.action_release("move_left")
	Input.action_release("move_right")
	Input.action_release("move_forward")
	Input.action_release("move_backward")
	queue_redraw()

func _release_action() -> void:
	_action_id = -1
	Input.action_release("interact")

func _draw() -> void:
	_action_center = Vector2(size.x - 110.0, size.y - 110.0)
	var base := _joystick_origin if _joystick_id != -1 else Vector2(120.0, size.y - 120.0)
	draw_circle(base, joystick_radius, Color(1, 1, 1, 0.12))
	draw_circle(base + _joystick_vector * joystick_radius, joystick_radius * 0.42, Color(1, 1, 1, 0.28))
	draw_circle(_action_center, action_radius, Color(1, 1, 1, 0.16))
	draw_circle(_action_center, action_radius * 0.62, Color(1, 1, 1, 0.28))

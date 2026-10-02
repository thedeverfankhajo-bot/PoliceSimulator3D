extends RefCounted
class_name TrafficViolation

enum Type {
	SPEEDING,
	RED_LIGHT,
	STOP_SIGN,
	WRONG_WAY
}

const TYPE_NAMES := {
	Type.SPEEDING: "سرعت غیرمجاز",
	Type.RED_LIGHT: "عبور از چراغ قرمز",
	Type.STOP_SIGN: "توقف نکردن پشت تابلوی ایست",
	Type.WRONG_WAY: "حرکت در مسیر خلاف"
}

var type: Type
var measured_value: float
var limit_value: float
var severity: int

func _init(violation_type: Type = Type.SPEEDING, measured: float = 0.0, limit: float = 0.0, violation_severity: int = 1) -> void:
	type = violation_type
	measured_value = measured
	limit_value = limit
	severity = maxi(1, violation_severity)

func get_title() -> String:
	return String(TYPE_NAMES.get(type, "تخلف نامشخص"))

func get_description() -> String:
	if type == Type.SPEEDING and limit_value > 0.0:
		return "%s: %.1f km/h در محدوده %.1f km/h" % [get_title(), measured_value, limit_value]
	return get_title()

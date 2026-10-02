extends StaticBody3D
class_name PoliceVehicle

signal entered(vehicle: PoliceVehicle)

@export var vehicle_id := "police_sedan"

func interact() -> void:
	entered.emit(self)

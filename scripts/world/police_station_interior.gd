extends Node3D
class_name PoliceStationInterior
## Mobile-friendly procedural interior dressing for the police station.
## No third-party binary assets are embedded.

func _ready() -> void:
	_build_floor()
	_build_front_desk()
	_build_workstations()
	_build_lockers()
	_build_lights()
	_build_evidence_area()

func _material(color: Color, roughness := 0.8, metallic := 0.0) -> StandardMaterial3D:
	var m := StandardMaterial3D.new()
	m.albedo_color = color
	m.roughness = roughness
	m.metallic = metallic
	return m

func _box(name_: String, size: Vector3, pos: Vector3, material: Material) -> MeshInstance3D:
	var mesh := MeshInstance3D.new()
	mesh.name = name_
	var box := BoxMesh.new()
	box.size = size
	mesh.mesh = box
	mesh.material_override = material
	mesh.position = pos
	add_child(mesh)
	return mesh

func _build_floor() -> void:
	_box("InteriorFloor", Vector3(9.4, 0.06, 11.4), Vector3(0, 0.03, 0), _material(Color(0.19, 0.21, 0.23), 0.92))
	_box("ReceptionRug", Vector3(4.0, 0.025, 2.2), Vector3(0, 0.075, 3.0), _material(Color(0.12, 0.20, 0.25), 0.95))

func _build_front_desk() -> void:
	var wood := _material(Color(0.25, 0.18, 0.12), 0.72)
	var top := _material(Color(0.72, 0.74, 0.70), 0.5)
	_box("ReceptionDesk", Vector3(4.4, 1.05, 0.7), Vector3(0, 0.55, 3.7), wood)
	_box("ReceptionTop", Vector3(4.6, 0.12, 0.8), Vector3(0, 1.12, 3.7), top)
	_box("ReceptionMonitor", Vector3(0.75, 0.5, 0.12), Vector3(-0.8, 1.42, 3.45), _material(Color(0.04, 0.12, 0.16), 0.2))

func _build_workstations() -> void:
	var desk := _material(Color(0.30, 0.22, 0.15), 0.76)
	var metal := _material(Color(0.25, 0.27, 0.30), 0.65, 0.25)
	for x in [-3.2, 3.2]:
		_box("WorkDesk", Vector3(2.3, 0.75, 1.0), Vector3(x, 0.42, -1.8), desk)
		_box("WorkScreen", Vector3(0.9, 0.55, 0.10), Vector3(x, 1.05, -1.75), _material(Color(0.04, 0.14, 0.18), 0.18))
		_box("Chair", Vector3(0.75, 0.85, 0.65), Vector3(x, 0.43, -3.0), metal)

func _build_lockers() -> void:
	var locker := _material(Color(0.34, 0.37, 0.40), 0.68, 0.18)
	for i in range(6):
		_box("Locker_%02d" % i, Vector3(0.85, 2.1, 0.55), Vector3(-4.0 + i * 1.0, 1.05, -4.5), locker)

func _build_lights() -> void:
	for x in [-3.0, 0.0, 3.0]:
		var light := OmniLight3D.new()
		light.name = "InteriorLight"
		light.position = Vector3(x, 3.7, 0)
		light.omni_range = 5.0
		light.light_energy = 1.0
		light.shadow_enabled = false
		add_child(light)

func _build_evidence_area() -> void:
	var cabinet := _material(Color(0.12, 0.15, 0.17), 0.65, 0.25)
	var evidence := _material(Color(0.62, 0.66, 0.68), 0.58)
	_box("EvidenceCabinet", Vector3(2.5, 2.2, 0.55), Vector3(2.8, 1.1, 1.0), cabinet)
	for i in range(4):
		_box("EvidenceShelf_%02d" % i, Vector3(1.9, 0.08, 0.35), Vector3(2.8, 0.45 + i * 0.4, 0.66), evidence)

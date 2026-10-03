extends Node3D
class_name CityExpansion
## Lightweight procedural 3D city dressing designed for the Mobile renderer.
## No external or unlicensed assets are embedded in the repository.

const BUILDING_MATERIALS := [Color(0.30, 0.34, 0.39), Color(0.42, 0.34, 0.29), Color(0.26, 0.38, 0.36), Color(0.46, 0.42, 0.35)]

func _ready() -> void:
	_build_blocks()
	_build_trees()
	_build_park()
	_build_police_lot()

func _mat(color: Color, roughness := 0.8) -> StandardMaterial3D:
	var m := StandardMaterial3D.new()
	m.albedo_color = color
	m.roughness = roughness
	return m

func _box(parent: Node, name_: String, size: Vector3, pos: Vector3, material: Material, collision := false) -> Node3D:
	var root: Node3D = StaticBody3D.new() if collision else Node3D.new()
	root.name = name_
	parent.add_child(root)
	root.position = pos
	if collision:
		var cs := CollisionShape3D.new()
		var shape := BoxShape3D.new()
		shape.size = size
		cs.shape = shape
		root.add_child(cs)
	var mesh := MeshInstance3D.new()
	var box := BoxMesh.new()
	box.size = size
	mesh.mesh = box
	mesh.material_override = material
	root.add_child(mesh)
	return root

func _building(pos: Vector3, size: Vector3, material: Material) -> void:
	_box(self, "CityBuilding", size, pos, material, true)
	var window_mat := _mat(Color(0.08, 0.20, 0.27), 0.25)
	var floors: int = max(1, int(size.y / 2.4))
	var columns: int = max(2, int(size.x / 2.0))
	for floor_index: int in range(floors):
		for column_index: int in range(columns):
			var x: float = -size.x * 0.5 + 0.8 + float(column_index) * ((size.x - 1.6) / float(max(1, columns - 1)))
			var y: float = 0.7 + float(floor_index) * 2.0
			var w: float = min(0.7, (size.x - 1.8) / float(max(1, columns)) * 0.55)
			var window := MeshInstance3D.new()
			var wm := BoxMesh.new()
			wm.size = Vector3(w, 0.75, 0.035)
			window.mesh = wm
			window.material_override = window_mat
			window.position = pos + Vector3(x, -size.y * 0.5 + y, -size.z * 0.5 - 0.02)
			add_child(window)

func _build_blocks() -> void:
	var blocks: Array[Array] = [
		[Vector3(-15, 4.0, -22), Vector3(8, 8, 10)], [Vector3(15, 3.0, -22), Vector3(8, 6, 10)],
		[Vector3(-15, 5.0, -7), Vector3(8, 10, 9)], [Vector3(15, 4.0, -7), Vector3(8, 8, 9)],
		[Vector3(-15, 3.5, 10), Vector3(8, 7, 11)], [Vector3(15, 5.5, 10), Vector3(8, 11, 11)],
		[Vector3(-15, 4.5, 27), Vector3(8, 9, 8)], [Vector3(15, 3.5, 27), Vector3(8, 7, 8)]
	]
	for i: int in range(blocks.size()):
		var b: Array = blocks[i]
		_building(b[0] as Vector3, b[1] as Vector3, _mat(BUILDING_MATERIALS[i % BUILDING_MATERIALS.size()]))

func _build_trees() -> void:
	var trunk_mat := _mat(Color(0.24, 0.15, 0.09))
	var leaf_mat := _mat(Color(0.12, 0.31, 0.18), 0.95)
	for z: int in range(-28, 31, 7):
		for x: float in [-8.0, 8.0]:
			var trunk := MeshInstance3D.new()
			var tm := CylinderMesh.new()
			tm.top_radius = 0.10
			tm.bottom_radius = 0.16
			tm.height = 1.8
			trunk.mesh = tm
			trunk.material_override = trunk_mat
			trunk.position = Vector3(x, 0.9, float(z))
			add_child(trunk)
			var crown := MeshInstance3D.new()
			var cm := SphereMesh.new()
			cm.radius = 0.9
			cm.height = 1.8
			crown.mesh = cm
			crown.material_override = leaf_mat
			crown.position = Vector3(x, 2.2, float(z))
			add_child(crown)

func _build_park() -> void:
	var grass := _mat(Color(0.17, 0.30, 0.17), 0.98)
	var path := _mat(Color(0.50, 0.48, 0.42), 0.92)
	_box(self, "Park", Vector3(7, 0.08, 13), Vector3(0, 0.05, 24), grass)
	_box(self, "ParkPath", Vector3(1.1, 0.10, 12), Vector3(0, 0.11, 24), path)

func _build_police_lot() -> void:
	var asphalt := _mat(Color(0.07, 0.08, 0.09), 0.96)
	_box(self, "PoliceLot", Vector3(10, 0.05, 10), Vector3(-12, 0.03, -14), asphalt)
	var white := _mat(Color(0.82, 0.82, 0.76), 0.75)
	for z: float in [-17.5, -14.0, -10.5]:
		_box(self, "ParkingLine", Vector3(7.0, 0.018, 0.08), Vector3(-12, 0.08, z), white)

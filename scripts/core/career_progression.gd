extends RefCounted
class_name CareerProgression

signal changed(xp: int, rank: String)

const RANKS := [
	{"name": "کارآموز", "xp": 0},
	{"name": "افسر", "xp": 100},
	{"name": "افسر ارشد", "xp": 300},
	{"name": "سرگروهبان", "xp": 650},
	{"name": "بازرس", "xp": 1200}
]

var xp: int = 0

func add_xp(amount: int) -> int:
	if amount <= 0:
		return xp
	xp += amount
	changed.emit(xp, get_rank())
	return xp

func get_rank() -> String:
	var result := String(RANKS[0]["name"])
	for rank in RANKS:
		if xp >= int(rank["xp"]):
			result = String(rank["name"])
	return result

func get_next_rank_xp() -> int:
	for rank in RANKS:
		if xp < int(rank["xp"]):
			return int(rank["xp"])
	return -1

func get_progress_to_next_rank() -> float:
	var next_xp := get_next_rank_xp()
	if next_xp < 0:
		return 1.0
	var previous_xp := 0
	for rank in RANKS:
		var rank_xp := int(rank["xp"])
		if rank_xp > xp:
			break
		previous_xp = rank_xp
	return clamp(float(xp - previous_xp) / max(float(next_xp - previous_xp), 1.0), 0.0, 1.0)

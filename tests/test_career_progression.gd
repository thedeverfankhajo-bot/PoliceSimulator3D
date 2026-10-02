extends SceneTree

const CAREER_SCRIPT := preload("res://scripts/core/career_progression.gd")

func _initialize() -> void:
	var career := CAREER_SCRIPT.new()
	assert(career.get_rank() == "کارآموز")
	career.add_xp(100)
	assert(career.get_rank() == "افسر")
	assert(career.get_next_rank_xp() == 300)
	career.add_xp(200)
	assert(career.get_rank() == "افسر ارشد")
	assert(is_equal_approx(career.get_progress_to_next_rank(), 0.0))
	career.add_xp(10000)
	assert(career.get_rank() == "بازرس")
	assert(career.get_next_rank_xp() == -1)
	assert(is_equal_approx(career.get_progress_to_next_rank(), 1.0))
	print("Career progression tests passed.")
	quit(0)

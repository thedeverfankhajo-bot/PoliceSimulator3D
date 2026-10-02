extends CanvasLayer
class_name MainMenu

signal start_requested
signal continue_requested
signal tutorial_requested
signal settings_requested

@onready var continue_button: Button = $Panel/VBox/Continue
@onready var settings_panel: Control = $SettingsPanel
@onready var tutorial_panel: Control = $TutorialPanel
@onready var master_slider: HSlider = $SettingsPanel/VBox/Master
@onready var music_slider: HSlider = $SettingsPanel/VBox/Music
@onready var sfx_slider: HSlider = $SettingsPanel/VBox/SFX
@onready var sensitivity_slider: HSlider = $SettingsPanel/VBox/Sensitivity
@onready var autosave: CheckButton = $SettingsPanel/VBox/Autosave
@onready var hints: CheckButton = $SettingsPanel/VBox/Hints
@onready var invert_y: CheckButton = $SettingsPanel/VBox/InvertY

func _ready() -> void:
	$Panel/VBox/NewGame.pressed.connect(func(): start_requested.emit())
	continue_button.pressed.connect(func(): continue_requested.emit())
	$Panel/VBox/Tutorial.pressed.connect(func(): _show_tutorial())
	$Panel/VBox/Settings.pressed.connect(func(): _show_settings())
	$Panel/VBox/Quit.pressed.connect(func(): get_tree().quit())
	$SettingsPanel/VBox/Back.pressed.connect(func(): _hide_panels())
	$TutorialPanel/VBox/Back.pressed.connect(func(): _hide_panels())
	$SettingsPanel/VBox/Reset.pressed.connect(_reset_settings)
	master_slider.value_changed.connect(func(v): $GameSettings.set_value("audio/master_db", v))
	music_slider.value_changed.connect(func(v): $GameSettings.set_value("audio/music_db", v))
	sfx_slider.value_changed.connect(func(v): $GameSettings.set_value("audio/sfx_db", v))
	sensitivity_slider.value_changed.connect(func(v): $GameSettings.set_value("gameplay/mouse_sensitivity", v))
	autosave.toggled.connect(func(v): $GameSettings.set_value("gameplay/auto_save", v))
	hints.toggled.connect(func(v): $GameSettings.set_value("gameplay/show_hints", v))
	invert_y.toggled.connect(func(v): $GameSettings.set_value("gameplay/invert_y", v))
	_load_settings_ui()
	continue_button.disabled = not $SaveManager.has_save()

func _load_settings_ui() -> void:
	master_slider.value = float($GameSettings.get_value("audio/master_db"))
	music_slider.value = float($GameSettings.get_value("audio/music_db"))
	sfx_slider.value = float($GameSettings.get_value("audio/sfx_db"))
	sensitivity_slider.value = float($GameSettings.get_value("gameplay/mouse_sensitivity"))
	autosave.button_pressed = bool($GameSettings.get_value("gameplay/auto_save"))
	hints.button_pressed = bool($GameSettings.get_value("gameplay/show_hints"))
	invert_y.button_pressed = bool($GameSettings.get_value("gameplay/invert_y"))

func _show_settings() -> void:
	$Panel.visible = false
	settings_panel.visible = true
	tutorial_panel.visible = false
	settings_requested.emit()

func _show_tutorial() -> void:
	$Panel.visible = false
	settings_panel.visible = false
	tutorial_panel.visible = true
	tutorial_requested.emit()

func _hide_panels() -> void:
	$Panel.visible = true
	settings_panel.visible = false
	tutorial_panel.visible = false

func _reset_settings() -> void:
	$GameSettings.reset()
	_load_settings_ui()

func close_menu() -> void:
	visible = false

func open_menu() -> void:
	visible = true
	$Panel.visible = true
	settings_panel.visible = false
	tutorial_panel.visible = false
	continue_button.disabled = not $SaveManager.has_save()

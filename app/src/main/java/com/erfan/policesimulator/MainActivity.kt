package com.erfan.policesimulator

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var game: GameEngine
    private lateinit var speed: TextView
    private lateinit var vehicleHealth: TextView
    private lateinit var status: TextView
    private lateinit var careerStatus: TextView
    private lateinit var objective: TextView
    private lateinit var radio: TextView
    private lateinit var sirenButton: Button
    private lateinit var vehicleButton: Button
    private lateinit var callButton: Button
    private lateinit var interactionButton: Button
    private lateinit var glView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        game = GameEngine(this)
        glView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(game)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
        val root = FrameLayout(this)
        root.addView(glView, FrameLayout.LayoutParams(-1, -1))
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; setPadding(12, 10, 12, 8) }
        val info = panel()
        info.addView(label("POLICE SIMULATOR 3D • UNIT 12", 15f, true))
        speed = label("0 KM/H", 22f, true); info.addView(speed)
        vehicleHealth = label("VEHICLE CONDITION: 100%", 12f, true); info.addView(vehicleHealth)
        status = label("DISPATCH AVAILABLE", 12f, true); info.addView(status)
        careerStatus = label("CAREER: LOADING PROFILE…", 11f, false); info.addView(careerStatus)
        top.addView(info, LinearLayout.LayoutParams(0, -2, 1f))
        val mission = panel()
        objective = label("Accept the dispatch call to begin.", 13f, true); mission.addView(objective)
        radio = label("Radio: Unit 12 available.", 11f, false); mission.addView(radio)
        top.addView(mission, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(top, FrameLayout.LayoutParams(-1, -2, Gravity.TOP))
        val controls = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(7, 6, 7, 10) }
        val steering = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val left = button("◀"); val right = button("▶")
        hold(left, { game.steer(-1f) }, { game.steer(0f) }); hold(right, { game.steer(1f) }, { game.steer(0f) })
        steering.addView(left, weight()); steering.addView(right, weight()); controls.addView(steering, LinearLayout.LayoutParams(0, -2, 1f))
        val drive = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val accel = button("ACCEL"); val brake = button("BRAKE")
        hold(accel, { game.gas(true) }, { game.gas(false) }); hold(brake, { game.brake(true) }, { game.brake(false) })
        drive.addView(accel, weight()); drive.addView(brake, weight()); controls.addView(drive, LinearLayout.LayoutParams(0, -2, 1.3f))
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        sirenButton = button("LIGHTS / SIREN") { game.toggleSiren() }
        vehicleButton = button("EXIT VEHICLE") {
            if (game.snapshot().decisionRequired) game.chooseCitation() else game.toggleVehicle()
        }
        interactionButton = button("INTERACT") {
            if (game.snapshot().decisionRequired) game.chooseArrest() else game.interact()
        }
        callButton = button("NEW CALL") { game.requestCall() }
        actions.addView(sirenButton, weight())
        actions.addView(vehicleButton, weight())
        actions.addView(interactionButton, weight())
        actions.addView(callButton, weight())
        controls.addView(actions, LinearLayout.LayoutParams(0, -2, 2.3f))
        root.addView(controls, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM))
        game.setListener { s, msg -> runOnUiThread {
            speed.text = s.speed.toString() + " KM/H"
            vehicleHealth.text = "VEHICLE CONDITION: ${s.vehicleCondition}%"
            vehicleHealth.setTextColor(when {
                s.vehicleCondition < 25 -> Color.rgb(255, 92, 72)
                s.vehicleCondition < 50 -> Color.rgb(255, 177, 66)
                else -> Color.WHITE
            })
            status.text = s.mission + if (s.onFoot) " • ON FOOT" else " • IN VEHICLE"
            careerStatus.text = "CAREER: ${s.career.xp} XP • CALLS: ${s.career.callsCompleted} • CORRECT: ${s.career.correctDecisions}/${s.career.callsCompleted}"
            objective.text = s.objective
            radio.text = if (msg.isNotBlank()) msg else s.radio
            sirenButton.text = if (s.lights && s.siren) "LIGHTS: ON" else "LIGHTS / SIREN"
            vehicleButton.text = when {
                s.decisionRequired -> "CITE / WARN"
                s.onFoot -> "ENTER VEHICLE"
                else -> "EXIT VEHICLE"
            }
            interactionButton.text = if (s.decisionRequired) "ARREST" else "INTERACT"
            callButton.text = if (s.score > 0) "NEW CALL • " + s.score + " XP" else "NEW CALL"
        } }
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        if (::glView.isInitialized) glView.onResume()
        if (::game.isInitialized) game.resumeAudio()
    }

    override fun onPause() {
        if (::game.isInitialized) game.pauseAudio()
        if (::glView.isInitialized) glView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        if (::game.isInitialized) game.releaseAudio()
        super.onDestroy()
    }

    private fun panel() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(10, 8, 10, 8); setBackgroundColor(Color.argb(215, 8, 20, 31)) }
    private fun label(text: String, size: Float, bold: Boolean) = TextView(this).apply { this.text = text; textSize = size; setTextColor(Color.WHITE); if (bold) setTypeface(Typeface.DEFAULT, Typeface.BOLD); setPadding(3, 3, 3, 3) }
    private fun button(text: String, action: () -> Unit = {}) = Button(this).apply { this.text = text; textSize = 10f; isAllCaps = false; setTextColor(Color.WHITE); setBackgroundColor(Color.rgb(25, 76, 105)); setOnClickListener { action() }; minHeight = (40 * resources.displayMetrics.density).toInt(); minimumHeight = minHeight }
    private fun weight() = LinearLayout.LayoutParams(0, -2, 1f).apply { setMargins(2, 2, 2, 2) }
    private fun hold(view: View, down: () -> Unit, up: () -> Unit) {
        view.setOnTouchListener { _, e -> when (e.actionMasked) { MotionEvent.ACTION_DOWN -> { down(); view.isPressed = true; true }; MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { up(); view.isPressed = false; true }; else -> true } }
    }
}

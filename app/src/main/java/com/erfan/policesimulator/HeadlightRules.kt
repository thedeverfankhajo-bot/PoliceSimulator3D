package com.erfan.policesimulator

internal enum class HeadlightMode(val label: String) {
    AUTO("AUTO"),
    ON("ON"),
    OFF("OFF")
}

/** Headlight control remains pure Kotlin so mode cycling and automatic night behavior are testable. */
internal object HeadlightRules {
    fun next(mode: HeadlightMode): HeadlightMode = when (mode) {
        HeadlightMode.AUTO -> HeadlightMode.ON
        HeadlightMode.ON -> HeadlightMode.OFF
        HeadlightMode.OFF -> HeadlightMode.AUTO
    }

    fun isOn(mode: HeadlightMode, lighting: WorldLighting): Boolean = when (mode) {
        HeadlightMode.AUTO -> lighting.streetLightsOn
        HeadlightMode.ON -> true
        HeadlightMode.OFF -> false
    }
}

package com.lsk0522.nightstand.core.common.model

/**
 * The colours a clock face can be tinted.
 *
 * Deliberately a fixed set rather than a free picker: every one of these reads
 * cleanly on black at a glance from across a room, which an arbitrary colour
 * does not. iOS offers the same kind of short list.
 */
enum class ClockColor(val argb: Long) {
    WHITE(0xFFFFFFFF),
    RED(0xFFFF453A),
    ORANGE(0xFFFF9F0A),
    YELLOW(0xFFFFD60A),
    GREEN(0xFF30D158),
    MINT(0xFF66D4CF),
    BLUE(0xFF0A84FF),
    PURPLE(0xFFBF5AF2),
    PINK(0xFFFF375F),
    ;

    companion object {
        val Default = WHITE
    }
}

/** How persistently StandBy should reappear while the charger stays connected. */
enum class StandbyPersistence {
    /** Shown once when charging starts. Picking the phone up ends it. */
    ONCE_PER_CHARGE,

    /** Comes back every time the phone is set down again. */
    WHILE_CHARGING,
}

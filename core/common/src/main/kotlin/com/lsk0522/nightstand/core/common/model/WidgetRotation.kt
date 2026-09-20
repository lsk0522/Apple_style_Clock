package com.lsk0522.nightstand.core.common.model

/**
 * How long a widget stays up before the stack moves on.
 *
 * A fixed set rather than a free number: this is read from across a room, and
 * anything under ten seconds turns the screen into something that flickers at
 * you rather than something you glance at.
 */
enum class WidgetRotationInterval(val seconds: Long) {
    TEN_SECONDS(10),
    THIRTY_SECONDS(30),
    ONE_MINUTE(60),
    FIVE_MINUTES(300),
    ;

    val millis: Long get() = seconds * 1_000L

    companion object {
        val Default = THIRTY_SECONDS
    }
}

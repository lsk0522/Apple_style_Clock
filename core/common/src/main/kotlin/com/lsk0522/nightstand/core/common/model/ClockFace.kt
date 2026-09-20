package com.lsk0522.nightstand.core.common.model

/**
 * The six StandBy clock styles, in the order iOS cycles through them.
 *
 * `Minimal Mono` was added after the original five; an earlier revision of the
 * plan listed a "Flip" face instead, which iOS does not have.
 */
enum class ClockFace {
    /** Hour stacked over minute, with the date alongside. */
    DIGITAL,

    /** Ticked dial with sweeping hands. */
    ANALOG,

    /** Local time plus a few other cities. */
    WORLD,

    /** The sun's position across the day, drawn as an arc. */
    SOLAR,

    /** Oversized rounded numerals in colour. */
    FLOAT,

    /** As little as possible: thin type, one weight, no colour. */
    MINIMAL_MONO,
    ;

    companion object {
        val Default = DIGITAL
    }
}

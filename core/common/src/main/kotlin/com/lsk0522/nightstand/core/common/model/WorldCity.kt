package com.lsk0522.nightstand.core.common.model

import android.icu.text.TimeZoneNames
import java.util.Locale

/**
 * The cities the World face can show.
 *
 * A fixed list rather than the platform's full zone database: `ZoneId` has
 * some six hundred entries, most of them administrative rather than places
 * anyone would pick, and a searchable six-hundred-row list is a worse answer
 * than two dozen cities that cover the offsets people actually care about.
 *
 * No display names are stored: see [displayName], which asks the platform
 * for each city's name in the user's own language.
 */
enum class WorldCity(val zoneId: String) {
    SEOUL("Asia/Seoul"),
    TOKYO("Asia/Tokyo"),
    SHANGHAI("Asia/Shanghai"),
    HONG_KONG("Asia/Hong_Kong"),
    SINGAPORE("Asia/Singapore"),
    BANGKOK("Asia/Bangkok"),
    JAKARTA("Asia/Jakarta"),
    KOLKATA("Asia/Kolkata"),
    DUBAI("Asia/Dubai"),
    MOSCOW("Europe/Moscow"),
    ISTANBUL("Europe/Istanbul"),
    BERLIN("Europe/Berlin"),
    PARIS("Europe/Paris"),
    LONDON("Europe/London"),
    LISBON("Europe/Lisbon"),
    SAO_PAULO("America/Sao_Paulo"),
    NEW_YORK("America/New_York"),
    TORONTO("America/Toronto"),
    CHICAGO("America/Chicago"),
    DENVER("America/Denver"),
    LOS_ANGELES("America/Los_Angeles"),
    ANCHORAGE("America/Anchorage"),
    HONOLULU("Pacific/Honolulu"),
    AUCKLAND("Pacific/Auckland"),
    SYDNEY("Australia/Sydney"),
    ;

    companion object {
        /** How many fit beside the time without crowding it. */
        const val MAX_SELECTED = 3

        val Default = listOf(NEW_YORK, LONDON, TOKYO)
    }
}

/**
 * The city's name in the user's language.
 *
 * ICU keeps an "exemplar location" for every zone — the city the zone is named
 * after, translated. Slicing the id instead would leave "Asia/Seoul" reading
 * as "Seoul" for a Korean user, which is the sort of detail that makes an app
 * feel imported.
 *
 * Falls back to the id when ICU has no name for it, which is better than an
 * empty row.
 */
fun WorldCity.displayName(): String = runCatching {
    TimeZoneNames.getInstance(Locale.getDefault()).getExemplarLocationName(zoneId)
}.getOrNull()?.takeIf { it.isNotBlank() }
    ?: zoneId.substringAfterLast('/').replace('_', ' ')

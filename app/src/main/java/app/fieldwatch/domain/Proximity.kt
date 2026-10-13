package app.fieldwatch.domain

/**
 * Plain-language reading of a signal strength. These bands are rules of thumb: walls, bodies,
 * antennas and the transmitter's own power move a radio between bands without it moving at all.
 * They are never shown as meters.
 */
enum class Proximity(val bars: Int, val label: String, val meaning: String) {
    VERY_CLOSE(
        4,
        "Very close",
        "Probably within arm's reach or on the same table, often in a hand, pocket or bag.",
    ),
    CLOSE(3, "Close", "Probably in the same room."),
    AREA(2, "In the area", "Probably another room, or around 10 meters away through walls."),
    FAR(1, "Far", "Far away, or behind several walls."),
    FAINT(0, "Barely heard", "Barely picked up. It could be very far away or blocked."),
}

object Proximities {
    const val NOT_DISTANCE = "This is a guess from signal strength, not a measurement of distance."

    /** Null when the radio has no usable reading (see [Rssi.measured]). */
    fun of(rssi: Int): Proximity? = when {
        !Rssi.measured(rssi) -> null
        rssi >= -50 -> Proximity.VERY_CLOSE
        rssi >= -65 -> Proximity.CLOSE
        rssi >= -75 -> Proximity.AREA
        rssi >= -85 -> Proximity.FAR
        else -> Proximity.FAINT
    }
}

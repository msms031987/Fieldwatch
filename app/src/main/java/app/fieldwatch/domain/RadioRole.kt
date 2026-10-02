package app.fieldwatch.domain

/**
 * What a radio means to the operator. Drives the one color the UI gives it:
 * red for attention, gold for presence, soft gold for vehicles, blue for infrastructure,
 * grey for unknown.
 */
enum class RadioRole {
    ATTENTION,
    PRESENCE,
    VEHICLE,
    INFRA,
    UNKNOWN,
}

object RadioRoles {
    /** Signatures whose class default is wrong for them. Keyed by built-in fleet id. */
    private val overrides: Map<String, RadioRole> = mapOf(
        // Action cameras travel with a person, unlike home security cameras.
        "fleet-gopro" to RadioRole.PRESENCE,
        "fleet-osmo" to RadioRole.PRESENCE,
        "fleet-insta360" to RadioRole.PRESENCE,
        // Headphones are worn; speakers (JBL, Sonos) stay infrastructure.
        "fleet-apple-audio" to RadioRole.PRESENCE,
        "fleet-sony" to RadioRole.PRESENCE,
        "fleet-bose" to RadioRole.PRESENCE,
        "fleet-shokz" to RadioRole.PRESENCE,
        // Handheld hospital barcode scanner, not a patient device.
        "fleet-honeywell-xenon-hc" to RadioRole.INFRA,
    )

    fun ofClass(kind: SignatureClass?): RadioRole = when (kind) {
        SignatureClass.HACKING,
        SignatureClass.BODYWORN,
        SignatureClass.LAW_ENFORCEMENT,
        SignatureClass.SURVEILLANCE,
        SignatureClass.DRONE,
        -> RadioRole.ATTENTION

        SignatureClass.PHONE,
        SignatureClass.WEARABLE,
        SignatureClass.GLASSES,
        SignatureClass.HEALTH,
        SignatureClass.FINDER,
        -> RadioRole.PRESENCE

        SignatureClass.VEHICLE -> RadioRole.VEHICLE

        SignatureClass.AUDIO,
        SignatureClass.CAMERA,
        SignatureClass.THERMOSTAT,
        SignatureClass.LOCK,
        SignatureClass.HOME,
        SignatureClass.ISP,
        SignatureClass.MESH,
        SignatureClass.BEACON,
        SignatureClass.SIGNAGE,
        -> RadioRole.INFRA

        SignatureClass.OTHER, null -> RadioRole.UNKNOWN
    }

    /** Extra attention text always wins; then a per-signature override; then the class default. */
    fun of(fleetId: String, kind: SignatureClass?, attentionNote: String): RadioRole = when {
        attentionNote.isNotBlank() -> RadioRole.ATTENTION
        else -> overrides[fleetId] ?: ofClass(kind)
    }

    /** Most urgent role among a radio's matched signatures; UNKNOWN when it matched none. */
    fun strongest(roles: Collection<RadioRole>): RadioRole = roles.minOrNull() ?: RadioRole.UNKNOWN
}

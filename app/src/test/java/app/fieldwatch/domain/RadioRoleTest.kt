package app.fieldwatch.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RadioRoleTest {
    private fun role(id: String): RadioRole {
        val f = DefaultCatalog.fleets().single { it.id == id }
        return RadioRoles.of(f.id, f.kind, f.attentionNote)
    }

    @Test
    fun attentionTextWinsOverClass() {
        assertEquals(RadioRole.ATTENTION, role("fleet-meta-glasses"))
        val glasses = DefaultCatalog.fleets().filter { it.kind == SignatureClass.GLASSES }
        glasses.forEach { assertEquals(it.id, RadioRole.ATTENTION, RadioRoles.of(it.id, it.kind, it.attentionNote)) }
    }

    @Test
    fun trackersAreGoldNotRed() {
        assertEquals(RadioRole.PRESENCE, role("fleet-airtag"))
    }

    @Test
    fun actionCamerasArePresenceHomeCamerasAreInfra() {
        assertEquals(RadioRole.PRESENCE, role("fleet-gopro"))
        assertEquals(RadioRole.PRESENCE, role("fleet-insta360"))
        assertEquals(RadioRole.INFRA, role("fleet-ring"))
    }

    @Test
    fun headphonesPresenceSpeakersInfra() {
        assertEquals(RadioRole.PRESENCE, role("fleet-sony"))
        assertEquals(RadioRole.INFRA, role("fleet-jbl"))
        assertEquals(RadioRole.INFRA, role("fleet-sonos"))
    }

    @Test
    fun vehiclesAndDrones() {
        assertEquals(RadioRole.VEHICLE, RadioRoles.ofClass(SignatureClass.VEHICLE))
        assertEquals(RadioRole.ATTENTION, RadioRoles.ofClass(SignatureClass.DRONE))
    }

    @Test
    fun unmatchedIsUnknownAndStrongestPicksMostUrgent() {
        assertEquals(RadioRole.UNKNOWN, RadioRoles.ofClass(null))
        assertEquals(RadioRole.UNKNOWN, RadioRoles.strongest(emptyList()))
        assertEquals(
            RadioRole.ATTENTION,
            RadioRoles.strongest(listOf(RadioRole.INFRA, RadioRole.ATTENTION, RadioRole.PRESENCE)),
        )
    }
}

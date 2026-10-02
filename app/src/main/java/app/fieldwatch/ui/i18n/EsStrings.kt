package app.fieldwatch.ui.i18n

import app.fieldwatch.domain.FieldwatchDisclaimer

/**
 * Spanish (voseo) for the UI strings that have been translated so far. The key is the exact
 * English text shown by the app; anything missing falls back to English. Keep entries sorted
 * by screen so gaps are easy to spot.
 */
object EsStrings {
    val map: Map<String, String> = mapOf(
        // Navigation and Live chrome
        "Live" to "En vivo",
        "Pause" to "Pausa",
        "Filters" to "Filtros",
        "Signatures" to "Firmas",
        "Reports" to "Informes",
        "Settings" to "Ajustes",
        "List" to "Lista",
        "Radar" to "Radar",
        "Classes" to "Clases",
        "Timeline" to "Tiempo",
        "SCANNING" to "ESCANEANDO",
        "PAUSED" to "EN PAUSA",
        "IDLE" to "INACTIVO",
        "DISPLAY" to "PANTALLA",

        // Section cards
        "Who stays" to "Quién permanece",
        "Watchlist" to "Lista de vigilancia",
        "TAK / CoT" to "TAK / CoT",
        "Source" to "Origen",
        "Sits" to "Sesiones",
        "Sit report" to "Informe de sesión",
        "Sit export" to "Exportar sesión",
        "Signature classes" to "Clases de firmas",
        "Settings backup" to "Copia de ajustes",
        "Selected signatures" to "Firmas elegidas",
        "Scanning" to "Escaneo",
        "Rules" to "Reglas",
        "Radios" to "Radios",
        "Preview" to "Vista previa",
        "Presets" to "Preajustes",
        "Path" to "Recorrido",
        "New detections" to "Detecciones nuevas",
        "Moving with you" to "Se mueve con vos",
        "Matching" to "Coincidencias",
        "Logging" to "Registro",
        "Log export" to "Exportar registro",
        "Location" to "Ubicación",
        "Identity" to "Identidad",
        "Fine filter" to "Filtro fino",
        "Fields" to "Campos",
        "Decode fields" to "Campos decodificados",
        "Compare sits" to "Comparar sesiones",
        "Color" to "Color",
        "Catalog" to "Catálogo",
        "Appearance" to "Apariencia",

        // Color roles
        "Attention" to "Atención",
        "Presence" to "Presencia",
        "Vehicle" to "Vehículo",
        "Infrastructure" to "Infraestructura",
        "Unknown or fading" to "Desconocido o desvaneciéndose",
        "Needs a look: alerts and flagged equipment." to "Merece una mirada: alertas y equipos marcados.",
        "Carried by people: phones, wearables, trackers, headphones." to "Lo llevan las personas: teléfonos, wearables, rastreadores, auriculares.",
        "Cars and their hotspots." to "Autos y sus puntos de acceso.",
        "Routers, mesh, home IoT, access control, speakers." to "Routers, mesh, IoT del hogar, control de acceso, parlantes.",
        "No signature, or not heard for a while." to "Sin firma, o sin escucharse hace un rato.",

        // Intro
        "Skip" to "Omitir",
        "Next" to "Siguiente",
        "Continue" to "Continuar",
        "Hear what your phone hears" to "Escuchá lo que escucha tu teléfono",
        "hears" to "escucha",
        "BISSA OpSec only listens. It lists the Wi-Fi access points and Bluetooth LE advertisers around you. No account, no server, nothing is transmitted." to
            "BISSA OpSec solo escucha. Lista los puntos de acceso Wi-Fi y los anunciantes Bluetooth LE a tu alrededor. Sin cuenta, sin servidor, no se transmite nada.",
        "Colors carry meaning" to "Los colores tienen significado",
        "meaning" to "significado",
        "One color per role, so the list reads at a glance." to "Un color por rol, para leer la lista de un vistazo.",
        "Read it your way" to "Leelo a tu manera",
        "way" to "manera",
        "Switch between List, Radar, Classes and Timeline. Use Filters to hide noise and Signatures to name what you find. On a tablet, turn on Sala mode in Settings." to
            "Cambiá entre Lista, Radar, Clases y Tiempo. Usá Filtros para ocultar ruido y Firmas para nombrar lo que encontrás. En una tablet, activá el modo Sala en Ajustes.",
        "Your data stays here" to "Tus datos se quedan acá",
        "stays" to "quedan",
        "Android needs Location, Nearby devices and Notifications to scan. Everything stays on this phone until you share it." to
            "Android necesita Ubicación, Dispositivos cercanos y Notificaciones para escanear. Todo queda en este teléfono hasta que lo compartas.",

        // Terms
        "Terms and license" to "Términos y licencia",
        "Before you start" to "Antes de empezar",
        "Disclaimer" to "Aviso legal",
        "The license text is kept in English." to "El texto de la licencia se mantiene en inglés.",
        "I have read this and I agree" to "Leí esto y estoy de acuerdo",
        FieldwatchDisclaimer.HOBBY to
            "Este es un proyecto personal, ofrecido tal cual bajo la licencia MIT. Usalo bajo tu propio riesgo.",
        FieldwatchDisclaimer.HYPOTHESES to
            "Las detecciones, las coincidencias de patrones, “Moving with you” / “posible seguimiento”, el texto de Debrief y la AI Export son hipótesis: no son identidad, ni un dictamen legal, ni una captura completa de radiofrecuencia. Los radios apagados, dormidos, con dirección aleatoria, solo celulares u ocultos por el sistema operativo no aparecerán.",
        FieldwatchDisclaimer.LIABILITY to
            "Sos el único responsable de cómo usás esta app y de cumplir la ley local. En la máxima medida permitida por la ley, los autores y colaboradores no son responsables por daños indirectos, incidentales, especiales, consecuentes o punitivos derivados de su uso.",
        FieldwatchDisclaimer.ACCEPT to
            "Al marcar la casilla y continuar, aceptás estos términos y la licencia MIT.",

        // Permissions
        "BISSA OpSec needs the radios" to "BISSA OpSec necesita las radios",
        "Location, nearby Wi-Fi, Bluetooth scan, and notifications let BISSA OpSec passively watch advertised networks and BLE devices. Nothing is transmitted." to
            "La ubicación, el Wi-Fi cercano, el escaneo Bluetooth y las notificaciones permiten que BISSA OpSec observe en forma pasiva las redes anunciadas y los dispositivos BLE. No se transmite nada.",
        "Grant permissions" to "Conceder permisos",

        // Settings: appearance
        "Language" to "Idioma",
        "Spanish covers the main screens, the intro and the terms. Text without a translation stays in English." to
            "El español cubre las pantallas principales, la intro y los términos. El texto sin traducir queda en inglés.",
        "Sala mode" to "Modo Sala",
        "Dense situation-room layout on Live: role counters, radar, list and attention feed together. Best on a tablet or in landscape." to
            "Diseño denso de sala de situación en En vivo: contadores por rol, radar, lista y alertas juntos. Ideal en tablet o en horizontal.",
        "Show intro again" to "Ver la intro otra vez",

        // Plain-language signal
        "Very close" to "Muy cerca",
        "Close" to "Cerca",
        "In the area" to "En la zona",
        "Far" to "Lejos",
        "Barely heard" to "Apenas se escucha",
        "Probably within arm's reach or on the same table, often in a hand, pocket or bag." to
            "Probablemente al alcance de la mano o en la misma mesa, a menudo en una mano, un bolsillo o una bolsa.",
        "Probably in the same room." to "Probablemente en el mismo cuarto.",
        "Probably another room, or around 10 meters away through walls." to
            "Probablemente en otro cuarto, o a unos 10 metros con paredes de por medio.",
        "Far away, or behind several walls." to "Lejos, o detrás de varias paredes.",
        "Barely picked up. It could be very far away or blocked." to
            "Apenas se capta. Puede estar muy lejos o bloqueado.",
        "This is a guess from signal strength, not a measurement of distance." to
            "Es una estimación a partir de la fuerza de la señal, no una medición de distancia.",
        "How to read the signal" to "Cómo leer la señal",
        "A stronger signal usually means the device is closer. Walls, bodies and the device itself change it, so use it as a hint." to
            "Una señal más fuerte suele indicar que el aparato está más cerca. Las paredes, los cuerpos y el propio aparato la alteran, así que usala como una pista.",
        "The radar" to "El radar",
        "The closer to the center, the stronger the signal. Where a dot sits around the circle does not show direction: the phone cannot tell which side a signal comes from." to
            "Cuanto más cerca del centro, más fuerte es la señal. El lugar donde un punto aparece alrededor del círculo no indica dirección: el teléfono no puede saber de qué lado viene una señal.",
        "To find a device" to "Para encontrar un aparato",
        "Walk and watch whether the signal rises or falls. That is more reliable than any single reading. Open a radio and use Hunt." to
            "Caminá y mirá si la señal sube o baja. Es más confiable que cualquier lectura aislada. Abrí un radio y usá Hunt.",
        "Got it" to "Entendido",
        "Closer to the center = stronger signal. Position around the circle does not show direction." to
            "Más cerca del centro = señal más fuerte. La posición alrededor del círculo no indica dirección.",
        "Plain-language signal" to "Señal en lenguaje claro",
        "On: signal shows as bars and words like Very close or Far, and the radar rings are labeled. Off: raw dBm numbers." to
            "Activado: la señal se muestra con barras y palabras como Muy cerca o Lejos, y los anillos del radar tienen nombre. Desactivado: números dBm.",
        "Signal is a hint, not a ruler" to "La señal es una pista, no una regla",
        "hint" to "pista",
        "A stronger signal usually means closer, but walls, bodies and the device itself change it. Walk toward a signal and see if it rises to be sure." to
            "Una señal más fuerte suele significar que está más cerca, pero las paredes, los cuerpos y el propio aparato la alteran. Caminá hacia una señal y mirá si sube para confirmarlo.",
        "Roughly" to "Aproximadamente",

        // Sala mode
        "SALA" to "SALA",
        "RADAR" to "RADAR",
        "RADIOS" to "RADIOS",
        "ATTENTION" to "ATENCIÓN",
        "on air" to "al aire",
        "Infra" to "Infra",
        "Unknown" to "Desconocido",
        "Nothing needs attention." to "Nada requiere atención.",
    )
}

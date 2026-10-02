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

        // Sweep
        "Sweep" to "Barrido",
        "Lenses and LEDs" to "Lentes y LEDs",
        "Magnetic field" to "Campo magnético",
        "Checklist" to "Lista de pasos",
        "Prototype. These tools help you decide where to look. They cannot prove a room is clean, and a camera that records to a card and never connects to anything can pass every one of them. Nothing from the camera or sensors is saved or sent." to
            "Prototipo. Estas herramientas ayudan a decidir dónde mirar. No pueden probar que un cuarto está limpio, y una cámara que graba en una tarjeta y nunca se conecta a nada puede pasarlas todas. Nada de la cámara ni de los sensores se guarda ni se envía.",
        "No camera-like radios heard right now." to "No se escuchan radios parecidos a cámaras ahora.",
        "camera-like radios heard right now." to "radios parecidos a cámaras escuchados ahora.",
        "These are Wi-Fi and Bluetooth signatures of known camera makers. Cameras that never join a network do not appear here." to
            "Son firmas de Wi-Fi y Bluetooth de fabricantes de cámaras conocidos. Las cámaras que nunca se conectan a una red no aparecen acá.",
        "Open Live" to "Abrir En vivo",
        "Lens finder" to "Buscador de lentes",
        "IR check" to "Revisión IR",
        "Lens finder: turn the room lights off. The torch blinks on and off: a lens throws the torch back, so its glint shows only while the torch is on. Lit LEDs, lamps and screens shine with the torch off too and are ignored. Sweep slowly across walls, shelves, outlets, clocks and smoke detectors." to
            "Buscador de lentes: apagá las luces del cuarto. La linterna parpadea: una lente devuelve la luz de la linterna, así que su destello aparece solo con la linterna encendida. Los LEDs encendidos, las lámparas y las pantallas brillan también con la linterna apagada y se ignoran. Recorré despacio paredes, estantes, enchufes, relojes y detectores de humo.",
        "IR check: in a dark room, some cameras show infrared LEDs as a faint bright dot. First test the camera: point a TV remote at the lens and press a button. If you see a light on screen, that camera sees infrared. Many cameras filter it out, so seeing nothing means little." to
            "Revisión IR: en un cuarto oscuro, algunas cámaras muestran los LEDs infrarrojos como un punto brillante tenue. Primero probá la cámara: apuntá un control remoto de TV a la lente y apretá un botón. Si ves una luz en la pantalla, esa cámara ve infrarrojo. Muchas lo filtran, así que no ver nada significa poco.",
        "Front camera" to "Cámara frontal",
        "Back camera" to "Cámara trasera",
        "The room is bright." to "El cuarto está iluminado.",
        "Turn off the lights for fewer false alarms." to "Apagá las luces para tener menos falsas alarmas.",
        "Dark enough." to "Suficientemente oscuro.",
        "This camera has no torch, so the blink test is off. Results will include lit LEDs and lamps." to
            "Esta cámara no tiene linterna, así que la prueba de parpadeo está desactivada. Los resultados incluirán LEDs encendidos y lámparas.",
        "Tap a marked spot to dismiss it while you hold still." to
            "Tocá un punto marcado para descartarlo mientras mantenés firme el teléfono.",
        "Show dismissed spots" to "Mostrar los puntos descartados",
        "Allow camera" to "Permitir la cámara",
        "Start" to "Iniciar",
        "Stop" to "Detener",
        "The camera could not start." to "No se pudo iniciar la cámara.",
        "steady bright spot(s). Look at the place with your own eyes: is there a small lens or LED?" to
            "punto(s) brillante(s) estable(s). Mirá el lugar con tus propios ojos: ¿hay una lente o un LED pequeño?",
        "A flicker. Hold steady on it." to "Un parpadeo. Mantené firme el teléfono ahí.",
        "Nothing yet. Move slowly. Reflections of lamps in glass can look like this too." to
            "Nada por ahora. Movete despacio. Los reflejos de lámparas en un vidrio también pueden verse así.",
        "This phone has no magnetic field sensor." to "Este teléfono no tiene sensor de campo magnético.",
        "Measures magnets, metal and electric currents near the phone. It does not detect radio waves, so it cannot find a transmitter. Set a baseline in a clear spot, then move slowly along furniture and walls and watch for a change." to
            "Mide imanes, metal y corrientes eléctricas cerca del teléfono. No detecta ondas de radio, así que no puede encontrar un transmisor. Fijá una referencia en un lugar despejado, después recorré despacio muebles y paredes y mirá si hay un cambio.",
        "No baseline yet. Tap Set baseline in a clear spot." to "Todavía no hay referencia. Tocá Fijar referencia en un lugar despejado.",
        "Normal: close to the baseline." to "Normal: cerca de la referencia.",
        "Changed: something magnetic or metal is near." to "Cambió: hay algo magnético o metálico cerca.",
        "Strong change: a magnet, speaker, motor or large piece of metal is very close." to
            "Cambio fuerte: un imán, un parlante, un motor o una pieza grande de metal está muy cerca.",
        "The reading is moving a lot: metal being moved, a motor or AC wiring nearby." to
            "La lectura se mueve mucho: metal en movimiento, un motor o cableado de corriente alterna cerca.",
        "Set baseline" to "Fijar referencia",
        "Smoke detectors, clocks, USB chargers and plugs: look for a tiny lens or LED." to
            "Detectores de humo, relojes, cargadores USB y enchufes: buscá una lente o un LED diminuto.",
        "Vents, shelves, plants and decorations that face the bed or the desk." to
            "Rejillas, estantes, plantas y adornos que miran hacia la cama o el escritorio.",
        "TV, set-top box and speakers: look for small holes or lenses." to
            "TV, decodificador y parlantes: buscá agujeros pequeños o lentes.",
        "Mirrors: touch the glass with a fingertip. If your finger touches its own reflection with no gap, it may be a two-way mirror." to
            "Espejos: tocá el vidrio con la punta de un dedo. Si el dedo toca su reflejo sin dejar espacio, puede ser un espejo de doble cara.",
        "Lights out: run the Lens finder, then the IR check, slowly around the room." to
            "Con las luces apagadas: usá el Buscador de lentes y después la Revisión IR, despacio por todo el cuarto.",
        "Radios: open Live and look for red or camera-like radios." to
            "Radios: abrí En vivo y buscá radios en rojo o parecidos a cámaras.",
        "Magnetic field: set a baseline, then sweep furniture and walls." to
            "Campo magnético: fijá una referencia y después recorré muebles y paredes.",
        "Anything plugged in with no clear purpose, or that is new since you last looked." to
            "Todo lo que esté enchufado sin un propósito claro, o que sea nuevo desde la última vez que miraste.",

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

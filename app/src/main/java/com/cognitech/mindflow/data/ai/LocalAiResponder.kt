package com.cognitech.mindflow.data.ai

/**
 * Respuesta empática generada en el dispositivo.
 * Es un placeholder hasta conectar el endpoint de chat del backend (GeminiService).
 */
class LocalAiResponder {

    fun detectSentiment(text: String): String {
        val t = text.lowercase()
        val tokens = t.split(Regex("[^\\p{L}]+")).filter { it.isNotEmpty() }
        val negatives = NEGATIVE_STEMS.count { stem -> tokens.any { it.startsWith(stem) } } +
            NEGATIVE_EXACT.count { word -> tokens.any { it == word } } +
            NEGATIVE_PHRASES.count { it in t }
        val positives = POSITIVE_STEMS.count { stem -> tokens.any { it.startsWith(stem) } } +
            POSITIVE_EXACT.count { word -> tokens.any { it == word } }
        return when {
            negatives > positives -> "negative"
            positives > negatives -> "positive"
            else -> "neutral"
        }
    }

    /** Título corto para la tarjeta del Journal: primera frase, máximo 40 caracteres. */
    fun title(text: String): String {
        val firstSentence = text.split('.', '!', '?', '\n').firstOrNull { it.isNotBlank() }?.trim() ?: text.trim()
        return if (firstSentence.length <= 40) firstSentence
        else firstSentence.take(40).substringBeforeLast(' ').trimEnd(',', ';', ':') + "..."
    }

    /** Frase de resumen para la tarjeta "Resumen Semanal" de Analíticas. */
    fun weeklyInsight(positive: Int, neutral: Int, negative: Int, topCategory: String?): String = when {
        positive + neutral + negative == 0 ->
            "Aún no tienes registros esta semana. Escribe cómo te sientes para recibir tu resumen."
        positive >= negative && positive >= neutral ->
            "Has mostrado una gran resiliencia esta semana." +
                (topCategory?.let { " La mayoría de tus registros fueron sobre $it y el balance es positivo." } ?: "")
        negative > positive ->
            "Esta semana ha sido exigente." +
                (topCategory?.let { " El estrés relacionado con ${it.lowercase()} aparece con frecuencia." } ?: "") +
                " Recuerda tomar pausas."
        else ->
            "Tu semana ha sido equilibrada. Registrar con constancia te ayudará a detectar patrones."
    }

    fun respond(text: String, sentiment: String): String {
        val t = text.lowercase()
        return when {
            listOf("ansie", "nervios", "pánico", "panico").any { it in t } ->
                "Percibo algo de ansiedad en lo que escribes. Es una reacción natural cuando algo nos importa. " +
                    "¿Te gustaría probar la respiración 4-7-8? Inhala 4 segundos, sostén 7 y exhala 8."
            listOf("trabajo", "jefe", "reunión", "reunion", "presentación", "presentacion").any { it in t } &&
                sentiment == "negative" ->
                "Noto algo de tensión en tus palabras sobre el trabajo hoy. Es completamente válido sentirse " +
                    "abrumado a veces. Recuerda que está bien tomar una pausa. ¿Te gustaría intentar un ejercicio " +
                    "rápido de respiración?"
            listOf("estudi", "examen", "curso", "tarea", "universidad").any { it in t } &&
                sentiment == "negative" ->
                "Parece que los estudios te están pesando. Avanzar poco también es avanzar. " +
                    "¿Qué tal si divides lo pendiente en bloques de 25 minutos con descansos cortos?"
            sentiment == "negative" ->
                "Gracias por compartir cómo te sientes. Lo que estás viviendo es válido. " +
                    "Tómate un momento para ti; a veces nombrar lo que sentimos ya es un primer paso."
            sentiment == "positive" ->
                "¡Qué bueno leer esto! Tómate un momento para reconocer lo que salió bien hoy. " +
                    "Registrar estos momentos te ayudará a volver a ellos cuando lo necesites."
            else ->
                "Gracias por escribir hoy. Registrar tus pensamientos con regularidad te ayuda a entenderte mejor. " +
                    "¿Hay algo más que quieras explorar?"
        }
    }

    private companion object {
        // Prefijos para cubrir conjugaciones/género ("cansad" -> cansado/cansada). Solo se
        // comparan contra tokens completos (nunca contra el texto crudo), así que no capturan
        // coincidencias accidentales a mitad de otra palabra.
        val NEGATIVE_STEMS = listOf(
            "triste", "cansad", "abrumad", "estrés", "estres", "ansie",
            "procrastin", "preocup", "frustr", "enoj", "agotad", "llor",
        )
        val POSITIVE_STEMS = listOf(
            "feliz", "content", "logré", "logre", "alegr", "orgullos", "tranquil", "motivad", "genial",
        )
        // Palabras cortas que calzarían como prefijo de palabras no relacionadas
        // ("mal" en "normal"/"animal", "bien" en "también", "sola" en "solamente"),
        // así que exigimos coincidencia exacta de token.
        val NEGATIVE_EXACT = listOf("mal", "sola", "miedo")
        val POSITIVE_EXACT = listOf("bien")
        val NEGATIVE_PHRASES = listOf("no avancé", "no avance")
    }
}

package com.cognitech.mindflow.data.repository

import android.content.ContentValues
import com.cognitech.mindflow.data.ai.LocalAiResponder
import com.cognitech.mindflow.data.local.MindFlowDatabase
import com.cognitech.mindflow.data.local.MindFlowDatabase.Companion.TABLE_JOURNAL
import com.cognitech.mindflow.data.model.JournalEntry
import com.cognitech.mindflow.data.remote.ApiClient
import com.cognitech.mindflow.data.remote.dto.CreateJournalEntryRequest
import com.cognitech.mindflow.data.remote.dto.JournalEntryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.OffsetDateTime

/**
 * La entrada se crea contra el backend (Journal), que genera la respuesta empática real con
 * Gemini. Si el backend no responde (sin red, backend caído), cae a la heurística local de
 * [LocalAiResponder] para no perder la entrada — el Journal sigue siendo local-first.
 */
class JournalRepository(
    private val database: MindFlowDatabase,
    private val aiResponder: LocalAiResponder,
) {

    suspend fun create(userId: Long, content: String, category: String): JournalEntry =
        withContext(Dispatchers.IO) {
            val text = content.trim()
            val title = aiResponder.title(text)
            // Cualquier falla de red, de la API o al parsear la respuesta cae aquí: nunca debe
            // impedir que la entrada se guarde, solo degrada a la heurística local.
            val remote = runCatching { createRemote(text, title, category) }.getOrNull()

            val sentiment = remote?.sentiment ?: aiResponder.detectSentiment(text)
            val aiResponse = remote?.aiResponse ?: aiResponder.respond(text, sentiment)
            val createdAt = remote?.createdAtMillis ?: System.currentTimeMillis()

            val values = ContentValues().apply {
                put("user_id", userId)
                put("title", title)
                put("content", text)
                put("category", category)
                put("sentiment", sentiment)
                put("ai_response", aiResponse)
                put("created_at", createdAt)
            }
            val id = database.writableDatabase.insertOrThrow(TABLE_JOURNAL, null, values)
            JournalEntry(id, userId, title, text, category, sentiment, aiResponse, createdAt)
        }

    private class RemoteJournalResult(val sentiment: String, val aiResponse: String?, val createdAtMillis: Long)

    private suspend fun createRemote(content: String, title: String, category: String): RemoteJournalResult? {
        val request = CreateJournalEntryRequest(
            date = LocalDate.now().toString(),
            title = title,
            content = content,
            sentiment = "auto",
            category = category,
        )
        val body = ApiClient.toJsonBody(CreateJournalEntryRequest.serializer(), request)
        val response = ApiClient.journalApi.createEntry(body)
        val responseBody = response.body() ?: return null
        if (!response.isSuccessful) return null
        val parsed = ApiClient.parseBody(JournalEntryResponse.serializer(), responseBody)
        val createdAtMillis = OffsetDateTime.parse(parsed.createdAt).toInstant().toEpochMilli()
        return RemoteJournalResult(parsed.sentiment, parsed.aiResponse, createdAtMillis)
    }

    suspend fun listByUser(userId: Long): List<JournalEntry> = withContext(Dispatchers.IO) {
        database.readableDatabase.query(
            TABLE_JOURNAL,
            arrayOf("id", "user_id", "title", "content", "category", "sentiment", "ai_response", "created_at"),
            "user_id = ?", arrayOf(userId.toString()), null, null, "created_at DESC",
        ).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        JournalEntry(
                            id = c.getLong(0),
                            userId = c.getLong(1),
                            title = c.getString(2),
                            content = c.getString(3),
                            category = c.getString(4),
                            sentiment = c.getString(5),
                            aiResponse = if (c.isNull(6)) null else c.getString(6),
                            createdAt = c.getLong(7),
                        )
                    )
                }
            }
        }
    }
}

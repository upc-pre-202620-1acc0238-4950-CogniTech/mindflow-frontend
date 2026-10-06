package com.cognitech.mindflow.application

import com.cognitech.mindflow.domain.model.ChatMessage
import com.cognitech.mindflow.domain.port.ChatResponder
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ChatUseCasesTest {
    private val responder = object : ChatResponder {
        override suspend fun reply(text: String) = ChatMessage("REPLY: $text", fromUser = false)
    }
    private val chat = ChatUseCases(responder)

    @Test fun `replies as the assistant`() {
        val reply = runBlocking { chat.reply("Estoy cansado") }
        assertEquals("REPLY: Estoy cansado", reply.text)
        assertFalse(reply.fromUser)
    }

    @Test fun `welcome message comes from the assistant`() {
        assertFalse(chat.welcome().fromUser)
    }
}

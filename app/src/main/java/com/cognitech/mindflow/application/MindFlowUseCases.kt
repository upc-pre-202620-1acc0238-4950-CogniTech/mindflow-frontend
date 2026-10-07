package com.cognitech.mindflow.application

import com.cognitech.mindflow.domain.model.*
import com.cognitech.mindflow.domain.port.*

class AuthUseCases(private val users: UserRepository, private val habits: HabitRepository, private val session: SessionPort) {
    // The current SQLite adapter keeps the legacy transaction (user + default habits + session)
    // atomic. Once Room replaces it, this use case owns that orchestration without API changes.
    suspend fun signUp(name: String, email: String, password: String): Result<User> = users.signUp(name, email, password)
    suspend fun signIn(email: String, password: String): Result<User> = users.signIn(email, password)
    suspend fun currentUser() = users.currentUser(); fun isLoggedIn() = session.isLoggedIn(); fun logout() = session.logout()
}
class JournalUseCases(private val journals: JournalRepository) { suspend fun create(userId: Long, content: String, category: String) = journals.create(userId, content, category); suspend fun list(userId: Long) = journals.listByUser(userId) }
class HabitUseCases(private val habits: HabitRepository) { suspend fun create(userId: Long, name: String, frequency: HabitFrequency) = habits.create(userId, name, frequency); suspend fun list(userId: Long) = habits.listByUser(userId); suspend fun toggleToday(id: Long) = habits.toggleToday(id); suspend fun history(userId: Long) = habits.history(userId) }
class ProfileUseCases(private val users: UserRepository, private val preferences: PreferencesPort) { suspend fun update(user: User, name: String, occupation: String, timezone: String) = users.updateProfile(user.id, name, occupation, timezone); suspend fun delete(user: User) = users.deleteAccount(user.id); var pinLock by preferences::pinLock; var darkMode by preferences::darkMode; var habitReminders by preferences::habitReminders }

class DashboardUseCases(private val journals: JournalRepository, private val habits: HabitRepository) {
    suspend fun load(userId: Long) = DashboardData(journals.listByUser(userId), habits.listByUser(userId))
    suspend fun saveEntry(userId: Long, content: String, category: String) = journals.create(userId, content, category)
    suspend fun toggleHabit(habitId: Long) = habits.toggleToday(habitId)
}
data class DashboardData(val entries: List<JournalEntry>, val habits: List<Habit>)

class ChatUseCases(private val chat: ChatResponder) {
    fun welcome() = ChatMessage(WELCOME_MESSAGE, fromUser = false)
    suspend fun reply(text: String) = chat.reply(text)
    private companion object { const val WELCOME_MESSAGE = "Hola, soy MindFlow AI. Estoy aquí para escucharte. ¿Cómo te sientes en este momento?" }
}

package com.cognitech.mindflow.infrastructure.adapter

import com.cognitech.mindflow.data.model.User as LegacyUser
import com.cognitech.mindflow.data.model.JournalEntry as LegacyJournalEntry
import com.cognitech.mindflow.data.model.Habit as LegacyHabit
import com.cognitech.mindflow.data.repository.AuthRepository
import com.cognitech.mindflow.data.repository.HabitRepository
import com.cognitech.mindflow.data.repository.JournalRepository
import com.cognitech.mindflow.domain.model.User
import com.cognitech.mindflow.domain.model.JournalEntry
import com.cognitech.mindflow.domain.model.Habit
import com.cognitech.mindflow.domain.model.HabitCategory
import com.cognitech.mindflow.domain.model.HabitFrequency
import com.cognitech.mindflow.domain.model.HabitLog
import com.cognitech.mindflow.domain.model.Sentiment
import com.cognitech.mindflow.domain.port.*
import java.time.LocalDate

/** Anti-corruption layer: isolates the legacy SQLite implementation from the domain. */
class SqliteUserAdapter(private val source: AuthRepository) : UserRepository {
    override suspend fun signUp(name: String, email: String, password: String) = source.signUp(name, email, password).map(::user)
    override suspend fun signIn(email: String, password: String) = source.signIn(email, password).map(::user)
    override suspend fun currentUser() = source.currentUser()?.let(::user)
    override suspend fun updateProfile(userId: Long, name: String, occupation: String, timezone: String) { source.updateProfile(userId, name, occupation, timezone) }
    override suspend fun setPlan(userId: Long, plan: String) { source.setPlan(userId, plan) }
    override suspend fun deleteAccount(userId: Long) { source.deleteAccount(userId) }
}
class SqliteJournalAdapter(private val source: JournalRepository) : com.cognitech.mindflow.domain.port.JournalRepository {
    override suspend fun create(userId: Long, content: String, category: String) = entry(source.create(userId, content, category))
    override suspend fun listByUser(userId: Long) = source.listByUser(userId).map(::entry)
}
class SqliteHabitAdapter(private val source: HabitRepository) : com.cognitech.mindflow.domain.port.HabitRepository {
    override suspend fun seedDefaults(userId: Long) { source.seedDefaults(userId) }
    override suspend fun create(userId: Long, name: String, frequency: HabitFrequency) { source.create(userId, name, frequency.label) }
    override suspend fun listByUser(userId: Long) = source.listByUser(userId).map(::habit)
    override suspend fun toggleToday(habitId: Long) { source.toggleToday(habitId) }
    override suspend fun history(userId: Long, limit: Int) = source.history(userId, limit).map { HabitLog(it.habitName, category(it.category), LocalDate.parse(it.date)) }
}
private fun user(x: LegacyUser) = User(x.id, x.email, x.name, x.occupation, x.timezone, x.plan, x.createdAt)
private fun entry(x: LegacyJournalEntry) = JournalEntry(x.id, x.userId, x.title, x.content, x.category, Sentiment.fromStorage(x.sentiment), x.aiResponse, x.createdAt)
private fun habit(x: LegacyHabit) = Habit(x.id, x.userId, x.name, category(x.category), HabitFrequency.fromStorage(x.frequency), x.createdAt, x.doneToday, x.streak)
private fun category(value: String) = HabitCategory.entries.firstOrNull { it.label == value } ?: HabitCategory.WELLNESS

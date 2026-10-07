package com.cognitech.mindflow.domain.port

import com.cognitech.mindflow.domain.model.*

interface UserRepository { suspend fun signUp(name: String, email: String, password: String): Result<User>; suspend fun signIn(email: String, password: String): Result<User>; suspend fun currentUser(): User?; suspend fun updateProfile(userId: Long, name: String, occupation: String, timezone: String); suspend fun deleteAccount(userId: Long) }
interface JournalRepository { suspend fun create(userId: Long, content: String, category: String): JournalEntry; suspend fun listByUser(userId: Long): List<JournalEntry> }
interface HabitRepository { suspend fun seedDefaults(userId: Long); suspend fun create(userId: Long, name: String, frequency: HabitFrequency); suspend fun listByUser(userId: Long): List<Habit>; suspend fun toggleToday(habitId: Long); suspend fun history(userId: Long, limit: Int = 30): List<HabitLog> }
interface SessionPort { fun isLoggedIn(): Boolean; fun login(userId: Long); fun logout(); fun currentUserId(): Long? }
interface PreferencesPort { var pinLock: Boolean; var darkMode: Boolean; var habitReminders: Boolean }
interface ChatResponder { suspend fun reply(text: String): ChatMessage }

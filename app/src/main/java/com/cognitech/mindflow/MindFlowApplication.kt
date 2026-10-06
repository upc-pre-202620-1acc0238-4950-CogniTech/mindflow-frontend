package com.cognitech.mindflow

import android.app.Application
import com.cognitech.mindflow.data.ai.LocalAiResponder
import com.cognitech.mindflow.data.local.MindFlowDatabase
import com.cognitech.mindflow.data.local.SessionManager
import com.cognitech.mindflow.data.remote.ApiClient
import com.cognitech.mindflow.data.repository.AuthRepository
import com.cognitech.mindflow.data.repository.HabitRepository
import com.cognitech.mindflow.data.repository.JournalRepository
import com.cognitech.mindflow.application.AuthUseCases
import com.cognitech.mindflow.application.HabitUseCases
import com.cognitech.mindflow.application.JournalUseCases
import com.cognitech.mindflow.application.ProfileUseCases
import com.cognitech.mindflow.application.DashboardUseCases
import com.cognitech.mindflow.application.ChatUseCases
import com.cognitech.mindflow.infrastructure.adapter.RemoteChatAdapter
import com.cognitech.mindflow.infrastructure.adapter.AndroidPreferencesAdapter
import com.cognitech.mindflow.infrastructure.adapter.AndroidSessionAdapter
import com.cognitech.mindflow.infrastructure.adapter.SqliteHabitAdapter
import com.cognitech.mindflow.infrastructure.adapter.SqliteJournalAdapter
import com.cognitech.mindflow.infrastructure.adapter.SqliteUserAdapter
import com.cognitech.mindflow.ui.theme.ThemeState

class MindFlowApplication : Application() {

    lateinit var authRepository: AuthRepository
        private set
    lateinit var journalRepository: JournalRepository
        private set
    lateinit var habitRepository: HabitRepository
        private set
    val aiResponder = LocalAiResponder()

    /** Composition root. Presentation must receive application use cases, never storage adapters. */
    lateinit var authUseCases: AuthUseCases
        private set
    lateinit var journalUseCases: JournalUseCases
        private set
    lateinit var habitUseCases: HabitUseCases
        private set
    lateinit var profileUseCases: ProfileUseCases
        private set
    lateinit var dashboardUseCases: DashboardUseCases
        private set
    lateinit var chatUseCases: ChatUseCases
        private set

    override fun onCreate() {
        super.onCreate()
        val database = MindFlowDatabase(this)
        habitRepository = HabitRepository(database)
        val sessionManager = SessionManager(this)
        ApiClient.init(sessionManager)
        ThemeState.isDark = sessionManager.darkMode
        authRepository = AuthRepository(database, sessionManager, habitRepository)
        journalRepository = JournalRepository(database, aiResponder)
        val session = AndroidSessionAdapter(sessionManager)
        authUseCases = AuthUseCases(SqliteUserAdapter(authRepository), SqliteHabitAdapter(habitRepository), session)
        journalUseCases = JournalUseCases(SqliteJournalAdapter(journalRepository))
        habitUseCases = HabitUseCases(SqliteHabitAdapter(habitRepository))
        profileUseCases = ProfileUseCases(SqliteUserAdapter(authRepository), AndroidPreferencesAdapter(sessionManager))
        dashboardUseCases = DashboardUseCases(SqliteJournalAdapter(journalRepository), SqliteHabitAdapter(habitRepository))
        chatUseCases = ChatUseCases(RemoteChatAdapter(aiResponder))
    }
}

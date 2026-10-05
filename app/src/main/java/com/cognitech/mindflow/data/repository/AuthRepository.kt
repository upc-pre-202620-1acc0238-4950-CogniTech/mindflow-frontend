package com.cognitech.mindflow.data.repository

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.cognitech.mindflow.data.local.MindFlowDatabase
import com.cognitech.mindflow.data.local.MindFlowDatabase.Companion.TABLE_USERS
import com.cognitech.mindflow.data.local.SessionManager
import com.cognitech.mindflow.data.model.User
import com.cognitech.mindflow.data.remote.ApiClient
import com.cognitech.mindflow.data.remote.dto.AuthenticatedUserResponse
import com.cognitech.mindflow.data.remote.dto.SignInRequest
import com.cognitech.mindflow.data.remote.dto.SignUpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * El inicio de sesión / registro ahora vive en el backend (IAM) — es la fuente de verdad de
 * identidad y la que emite el JWT. La tabla local `users` queda como caché offline del perfil
 * (lo que permite a Journal/Habits, que siguen siendo 100% locales por ahora, seguir operando
 * sin red). Sincronizar Journal/Habits con el backend queda pendiente para un paso posterior.
 */
class AuthRepository(
    private val database: MindFlowDatabase,
    val session: SessionManager,
    private val habitRepository: HabitRepository,
) {

    suspend fun signUp(name: String, email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val trimmedEmail = email.trim().lowercase()
                val signUpBody = ApiClient.toJsonBody(
                    SignUpRequest.serializer(), SignUpRequest(trimmedEmail, password, name.trim())
                )
                val signUpResponse = ApiClient.authApi.signUp(signUpBody)
                if (!signUpResponse.isSuccessful) {
                    return@withContext Result.failure(
                        AuthException(ApiClient.errorMessage(signUpResponse) ?: "No se pudo crear la cuenta")
                    )
                }

                // El backend no emite un JWT en /sign-up; se inicia sesión justo después para obtenerlo.
                val signInBody = ApiClient.toJsonBody(SignInRequest.serializer(), SignInRequest(trimmedEmail, password))
                val signInResponse = ApiClient.authApi.signIn(signInBody)
                val signInBodyResult = signInResponse.body()
                if (!signInResponse.isSuccessful || signInBodyResult == null) {
                    return@withContext Result.failure(
                        AuthException("Cuenta creada, pero no se pudo iniciar sesión automáticamente.")
                    )
                }
                val auth = ApiClient.parseBody(AuthenticatedUserResponse.serializer(), signInBodyResult)

                val userId = auth.id.toLong()
                cacheUser(userId, auth.email, password, name.trim())
                habitRepository.seedDefaults(userId)
                session.login(userId, auth.token)
                Result.success(findById(userId)!!)
            } catch (e: IOException) {
                Result.failure(AuthException("No se pudo conectar con el servidor. Verifica tu conexión."))
            }
        }

    suspend fun signIn(email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val trimmedEmail = email.trim().lowercase()
                val body = ApiClient.toJsonBody(SignInRequest.serializer(), SignInRequest(trimmedEmail, password))
                val response = ApiClient.authApi.signIn(body)
                val responseBody = response.body()
                if (!response.isSuccessful || responseBody == null) {
                    return@withContext Result.failure(
                        AuthException(ApiClient.errorMessage(response) ?: "Correo o contraseña incorrectos")
                    )
                }
                val auth = ApiClient.parseBody(AuthenticatedUserResponse.serializer(), responseBody)

                val userId = auth.id.toLong()
                val isNewLocalAccount = findById(userId) == null
                cacheUser(userId, auth.email, password)
                if (isNewLocalAccount) habitRepository.seedDefaults(userId)
                session.login(userId, auth.token)
                Result.success(findById(userId)!!)
            } catch (e: IOException) {
                Result.failure(AuthException("No se pudo conectar con el servidor. Verifica tu conexión."))
            }
        }

    suspend fun currentUser(): User? = withContext(Dispatchers.IO) {
        session.currentUserId?.let(::findById)
    }

    suspend fun updateProfile(userId: Long, name: String, occupation: String, timezone: String) =
        withContext(Dispatchers.IO) {
            val values = ContentValues().apply {
                put("name", name.trim())
                put("occupation", occupation.trim())
                put("timezone", timezone.trim())
            }
            database.writableDatabase.update(TABLE_USERS, values, "id = ?", arrayOf(userId.toString()))
        }

    suspend fun setPlan(userId: Long, plan: String) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply { put("plan", plan) }
        database.writableDatabase.update(TABLE_USERS, values, "id = ?", arrayOf(userId.toString()))
    }

    suspend fun deleteAccount(userId: Long) = withContext(Dispatchers.IO) {
        // TODO: por ahora solo borra el caché local; falta el DELETE /api/v1/users contra el backend.
        database.writableDatabase.delete(TABLE_USERS, "id = ?", arrayOf(userId.toString()))
        session.logout()
    }

    fun isLoggedIn(): Boolean = session.currentUserId != null

    fun logout() = session.logout()

    /** Upsert del perfil remoto en el caché local (tabla `users`), preservando campos solo-locales existentes. */
    private fun cacheUser(id: Long, email: String, password: String, name: String? = null) {
        val existing = findById(id)
        val values = ContentValues().apply {
            put("id", id)
            put("email", email)
            put("name", name ?: existing?.name ?: email.substringBefore("@"))
            put("password_hash", hashPassword(password))
            put("occupation", existing?.occupation ?: "")
            put("timezone", existing?.timezone ?: "GMT-5 (Lima, Perú)")
            put("plan", existing?.plan ?: User.PLAN_FREEMIUM)
            put("created_at", existing?.createdAt ?: System.currentTimeMillis())
        }
        database.writableDatabase.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    private fun findById(id: Long): User? = database.readableDatabase.query(
        TABLE_USERS,
        arrayOf("id", "email", "name", "occupation", "timezone", "plan", "created_at"),
        "id = ?", arrayOf(id.toString()), null, null, null,
    ).use { if (it.moveToFirst()) it.toUser() else null }

    private fun Cursor.toUser() = User(
        id = getLong(0),
        email = getString(1),
        name = getString(2),
        occupation = getString(3),
        timezone = getString(4),
        plan = getString(5),
        createdAt = getLong(6),
    )

    // salt:hash en hex con SHA-256. Mirror local de la contraseña para el caché offline;
    // la validación real ahora ocurre en el backend (BCrypt) en cada sign-in.
    private fun hashPassword(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        return salt.toHex() + ":" + sha256(salt + password.toByteArray()).toHex()
    }

    private fun sha256(bytes: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(bytes)

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}

class AuthException(message: String) : Exception(message)

package com.example.siidm.data.network

import android.content.Context
import android.content.SharedPreferences
import com.example.siidm.data.model.User

object SessionManager {

    private const val PREFS_NAME = "docencia1_prefs"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_ROL = "rol"
    private const val KEY_IS_LOGGED = "is_logged"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(user: User) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED, true)
            putString(KEY_TOKEN, user.token)
            putInt(KEY_USER_ID, user.id)
            putString(KEY_USERNAME, user.username)
            putString(KEY_ROL, user.rol)
            apply()
        }
    }

    fun getToken(): String? =
        if (::prefs.isInitialized) prefs.getString(KEY_TOKEN, null) else null

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun getRol(): String = prefs.getString(KEY_ROL, "") ?: ""

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED, false)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
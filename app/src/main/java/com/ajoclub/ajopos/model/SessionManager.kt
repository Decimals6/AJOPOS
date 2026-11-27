package com.ajoclub.ajopos.model

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefsName = "petung_session"
    private val prefs: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_ROLE = "role" // "admin" or "pos"
        private const val KEY_USERNAME = "username"
    }

    fun saveLogin(userId: Int, username: String, role: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_ROLE, role)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)
    fun getRole(): String? = prefs.getString(KEY_ROLE, null)
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
}

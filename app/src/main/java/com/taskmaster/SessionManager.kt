package com.taskmaster

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "taskmaster_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USERNAME = "username"
        private const val KEY_PHONE = "phone"
        private const val KEY_BIRTHDATE = "birthdate"
    }

    // Guardar sesión al hacer login
    fun saveLoginSession(email: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    // Guardar info personal del registro
    fun savePersonalInfo(
        nombre: String,
        apellidos: String,
        username: String,
        phone: String,
        birthdate: String
    ) {
        prefs.edit()
            .putString(KEY_USER_NAME, "$nombre $apellidos")
            .putString(KEY_USERNAME, username)
            .putString(KEY_PHONE, phone)
            .putString(KEY_BIRTHDATE, birthdate)
            .apply()
    }

    // Verificar si hay sesión activa
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    // Obtener datos del usuario
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""
    fun getPhone(): String = prefs.getString(KEY_PHONE, "") ?: ""
    fun getBirthdate(): String = prefs.getString(KEY_BIRTHDATE, "") ?: ""

    // Cerrar sesión
    fun logout() {
        prefs.edit().clear().apply()
    }
}
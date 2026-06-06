package com.taskmaster.core.repositories

data class UserProfile(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val userName: String = "",
    val phone: String = "",
    val birthDate: String = ""
)
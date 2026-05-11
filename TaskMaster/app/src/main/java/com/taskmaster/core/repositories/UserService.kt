package com.taskmaster.core.repositories

import com.taskmaster.core.ResponseService

interface UserService {
    suspend fun saveUserInfo(userProfile: UserProfile): ResponseService<Unit>
    suspend fun getUserInfo(uid: String): ResponseService<UserProfile>
}

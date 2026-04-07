// app/src/main/java/com/taskmaster/data/Task.kt
package com.taskmaster.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val dueDate: String,
    val priority: String,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
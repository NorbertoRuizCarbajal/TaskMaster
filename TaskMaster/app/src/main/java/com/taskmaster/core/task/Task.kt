package com.taskmaster.core.task

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String,
    val dueDate: String,
    val priority: String,
    val priorityLevel: Int = 1,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val reminderId: String? = null
) : Parcelable {
    companion object {
        fun priorityToLevel(priority: String): Int = when (priority.lowercase()) {
            "alta"  -> 3
            "media" -> 2
            else    -> 1
        }
    }
}

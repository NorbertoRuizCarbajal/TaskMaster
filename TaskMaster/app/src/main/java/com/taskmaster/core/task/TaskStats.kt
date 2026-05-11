package com.taskmaster.core.task

import androidx.room.ColumnInfo

data class TaskCategoryStat(
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "total") val total: Int,
    @ColumnInfo(name = "completed") val completed: Int
)

data class TaskDateStat(
    @ColumnInfo(name = "dueDate") val dueDate: String,
    @ColumnInfo(name = "total") val total: Int,
    @ColumnInfo(name = "completed") val completed: Int
)

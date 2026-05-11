package com.taskmaster.core.task

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isDone ASC, createdAt DESC")
    fun getAllTasks(): LiveData<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks")
    fun getTotal(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isDone = 1")
    fun getTotalCompleted(): LiveData<Int>

    @Query("""
        SELECT category,
        COUNT(*) as total,
        SUM(CASE WHEN isDone = 1 THEN 1 ELSE 0 END) as completed
        FROM tasks GROUP BY category
    """)
    fun getStatsByCategory(): LiveData<List<TaskCategoryStat>>

    @Query("""
        SELECT dueDate,
        COUNT(*) as total,
        SUM(CASE WHEN isDone = 1 THEN 1 ELSE 0 END) as completed
        FROM tasks GROUP BY dueDate
    """)
    fun getStatsByDate(): LiveData<List<TaskDateStat>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}

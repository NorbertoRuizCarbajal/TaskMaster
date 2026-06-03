package com.taskmaster.core.task

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY isDone ASC, priority DESC, createdAt DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE isDone = 0 ORDER BY dueDate ASC")
    fun getPendingTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY isDone ASC")
    fun getTasksByCategory(category: String): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM tasks")
    fun getTotal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tasks WHERE isDone = 1")
    fun getTotalCompleted(): Flow<Int>

    @Query("""
        SELECT category,
        COUNT(*) as total,
        SUM(CASE WHEN isDone = 1 THEN 1 ELSE 0 END) as completed
        FROM tasks GROUP BY category
    """)
    fun getStatsByCategory(): Flow<List<TaskCategoryStat>>

    @Query("""
        SELECT dueDate,
        COUNT(*) as total,
        SUM(CASE WHEN isDone = 1 THEN 1 ELSE 0 END) as completed
        FROM tasks GROUP BY dueDate
    """)
    fun getStatsByDate(): Flow<List<TaskDateStat>>

    // NUEVO: buscar tareas pendientes de un día específico para WorkManager
    @Query("SELECT * FROM tasks WHERE dueDate = :date AND isDone = 0")
    suspend fun getPendingTasksForDate(date: String): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Int)
}
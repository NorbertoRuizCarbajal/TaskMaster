package com.taskmaster.core.task

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    val allTasks: Flow<List<Task>> = dao.getAllTasks()
    val pendingTasks: Flow<List<Task>> = dao.getPendingTasks()
    val total: Flow<Int> = dao.getTotal()
    val totalCompleted: Flow<Int> = dao.getTotalCompleted()
    val statsByCategory: Flow<List<TaskCategoryStat>> = dao.getStatsByCategory()
    val statsByDate: Flow<List<TaskDateStat>> = dao.getStatsByDate()

    suspend fun insert(task: Task): Long = dao.insert(task)
    suspend fun update(task: Task) = dao.update(task)
    suspend fun delete(task: Task) = dao.delete(task)
    suspend fun deleteById(id: Int) = dao.deleteById(id)
    suspend fun getById(id: Int): Task? = dao.getById(id)
    suspend fun toggleDone(task: Task) = dao.update(task.copy(isDone = !task.isDone))
    suspend fun getPendingTasksForDate(date: String) = dao.getPendingTasksForDate(date)
    fun getTasksByCategory(category: String): Flow<List<Task>> = dao.getTasksByCategory(category)
    fun getTasksByDate(date: String): Flow<List<Task>> = dao.getTasksByDate(date)
    fun searchTasks(query: String): Flow<List<Task>> = dao.searchTasks(query)
}
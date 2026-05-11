package com.taskmaster.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.taskmaster.core.task.Task
import com.taskmaster.core.task.TaskCategoryStat
import com.taskmaster.core.task.TaskDatabase
import com.taskmaster.core.task.TaskDateStat
import kotlinx.coroutines.launch

class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = TaskDatabase.getDatabase(app).taskDao()

    val allTasks: LiveData<List<Task>> = dao.getAllTasks()
    val total: LiveData<Int> = dao.getTotal()
    val totalCompleted: LiveData<Int> = dao.getTotalCompleted()
    val statsByCategory: LiveData<List<TaskCategoryStat>> = dao.getStatsByCategory()
    val statsByDate: LiveData<List<TaskDateStat>> = dao.getStatsByDate()

    fun insert(task: Task) = viewModelScope.launch { dao.insert(task) }
    fun update(task: Task) = viewModelScope.launch { dao.update(task) }
    fun delete(task: Task) = viewModelScope.launch { dao.delete(task) }
    fun toggleDone(task: Task) = viewModelScope.launch { dao.update(task.copy(isDone = !task.isDone)) }
}

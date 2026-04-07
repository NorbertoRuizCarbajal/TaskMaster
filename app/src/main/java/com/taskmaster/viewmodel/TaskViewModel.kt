// app/src/main/java/com/taskmaster/viewmodel/TaskViewModel.kt
package com.taskmaster.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.taskmaster.data.Task
import com.taskmaster.data.TaskDatabase
import kotlinx.coroutines.launch

class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = TaskDatabase.getDatabase(app).taskDao()

    val allTasks: LiveData<List<Task>> = dao.getAllTasks()

    fun insert(task: Task) = viewModelScope.launch { dao.insert(task) }
    fun update(task: Task) = viewModelScope.launch { dao.update(task) }
    fun delete(task: Task) = viewModelScope.launch { dao.delete(task) }

    fun toggleDone(task: Task) = viewModelScope.launch {
        dao.update(task.copy(isDone = !task.isDone))
    }
}
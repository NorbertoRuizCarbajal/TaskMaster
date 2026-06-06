package com.taskmaster.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taskmaster.core.task.Task
import com.taskmaster.core.task.TaskCategoryStat
import com.taskmaster.core.task.TaskDatabase
import com.taskmaster.core.task.TaskDateStat
import com.taskmaster.core.task.TaskReminderWorker
import com.taskmaster.core.task.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class TaskUiState {
    data object Loading : TaskUiState()
    data class Success(val tasks: List<Task>) : TaskUiState()
    data class Error(val message: String) : TaskUiState()
    data object Empty : TaskUiState()
}

class TaskViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = TaskRepository(
        TaskDatabase.getDatabase(app).taskDao()
    )

    private val _uiState = MutableStateFlow<TaskUiState>(TaskUiState.Loading)
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    val total: StateFlow<Int> = repository.total
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val totalCompleted: StateFlow<Int> = repository.totalCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val statsByCategory: StateFlow<List<TaskCategoryStat>> = repository.statsByCategory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val statsByDate: StateFlow<List<TaskDateStat>> = repository.statsByDate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _navigateToDetail = MutableSharedFlow<Task>(replay = 0)
    val navigateToDetail: SharedFlow<Task> = _navigateToDetail.asSharedFlow()

    init {
        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            repository.allTasks
                .catch { e ->
                    _uiState.value = TaskUiState.Error(e.message ?: "Error desconocido")
                }
                .collect { tasks ->
                    _uiState.value = when {
                        tasks.isEmpty() -> TaskUiState.Empty
                        else -> TaskUiState.Success(tasks)
                    }
                }
        }
    }

    fun insert(task: Task) = viewModelScope.launch {
        try { repository.insert(task) }
        catch (e: Exception) { _uiState.value = TaskUiState.Error("Error al guardar: ${e.message}") }
    }

    fun update(task: Task) = viewModelScope.launch {
        try { repository.update(task) }
        catch (e: Exception) { _uiState.value = TaskUiState.Error("Error al actualizar: ${e.message}") }
    }

    fun delete(task: Task) = viewModelScope.launch {
        try { repository.delete(task) }
        catch (e: Exception) { _uiState.value = TaskUiState.Error("Error al eliminar: ${e.message}") }
    }

    fun toggleDone(task: Task) = viewModelScope.launch {
        try { repository.toggleDone(task) }
        catch (e: Exception) { _uiState.value = TaskUiState.Error("Error al actualizar estado") }
    }

    fun onTaskClicked(task: Task) = viewModelScope.launch {
        _navigateToDetail.emit(task)
    }

    fun scheduleReminder(task: Task, delayMinutes: Long = 30) {
        TaskReminderWorker.scheduleReminder(
            context = getApplication(),
            taskId = task.id,
            taskName = task.name,
            delayMinutes = delayMinutes
        )
    }

    fun cancelReminder(task: Task) {
        TaskReminderWorker.cancelReminder(getApplication(), task.id)
    }
}
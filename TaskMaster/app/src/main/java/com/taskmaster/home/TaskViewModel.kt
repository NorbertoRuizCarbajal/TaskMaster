package com.taskmaster.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.taskmaster.core.task.Task
import com.taskmaster.core.task.TaskCategoryStat
import com.taskmaster.core.task.TaskDatabase
import com.taskmaster.core.task.TaskDateStat
import com.taskmaster.core.task.TaskRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class TaskUiState {
    data object Loading : TaskUiState()
    data class Success(val tasks: List<Task>) : TaskUiState()
    data class Error(val message: String) : TaskUiState()
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


    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {

        observeTasks()
    }

    private fun observeTasks() {
        viewModelScope.launch {
            repository.allTasks
                .catch { e ->
                    // BUG CORREGIDO: antes los errores de Flow silenciosos crasheaban la app
                    _uiState.value = TaskUiState.Error(e.message ?: "Error desconocido")
                }
                .collect { tasks ->
                    _uiState.value = TaskUiState.Success(tasks)
                }
        }
    }

    fun setCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun insert(task: Task) = viewModelScope.launch {
        try { repository.insert(task) }
        catch (e: Exception) { /* loggear en producción */ }
    }

    fun update(task: Task) = viewModelScope.launch { repository.update(task) }

    fun delete(task: Task) = viewModelScope.launch { repository.delete(task) }

    fun toggleDone(task: Task) = viewModelScope.launch { repository.toggleDone(task) }
}
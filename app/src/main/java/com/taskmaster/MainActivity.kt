// app/src/main/java/com/taskmaster/MainActivity.kt
package com.taskmaster

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.taskmaster.data.Task
import com.taskmaster.databinding.ActivityMainBinding
import com.taskmaster.ui.TaskAdapter
import com.taskmaster.viewmodel.TaskViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]

        adapter = TaskAdapter(
            onToggle = { viewModel.toggleDone(it) },
            onDelete = { viewModel.delete(it) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.allTasks.observe(this) { tasks ->
            adapter.submitList(tasks)
            val done = tasks.count { it.isDone }
            binding.tvProgress.text = "$done de ${tasks.size} completadas"
        }

        binding.fab.setOnClickListener { showAddTaskDialog() }
    }

    private fun showAddTaskDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_task, null)
        MaterialAlertDialogBuilder(this)
            .setTitle("Nueva tarea")
            .setView(view)
            .setPositiveButton("Agregar") { _, _ ->
                val name = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_name).text.toString()
                val date = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_date).text.toString()
                val catGroup = view.findViewById<ChipGroup>(R.id.chip_group_cat)
                val prioGroup = view.findViewById<ChipGroup>(R.id.chip_group_prio)

                val cat = when (catGroup.checkedChipId) {
                    R.id.chip_personal -> "Personal"
                    R.id.chip_salud    -> "Salud"
                    else               -> "Trabajo"
                }
                val prio = when (prioGroup.checkedChipId) {
                    R.id.chip_alta -> "alta"
                    R.id.chip_baja -> "baja"
                    else           -> "media"
                }
                if (name.isNotBlank()) {
                    viewModel.insert(Task(name = name, category = cat, dueDate = date.ifBlank { "Hoy" }, priority = prio))
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
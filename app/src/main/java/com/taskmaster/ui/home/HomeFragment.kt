package com.taskmaster.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taskmaster.R
import com.taskmaster.data.Task
import com.taskmaster.databinding.FragmentHomeBinding
import com.taskmaster.viewmodel.TaskViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        adapter = TaskAdapter(
            onToggle = { viewModel.toggleDone(it) },
            onDelete = { viewModel.delete(it) }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            val done = tasks.count { it.isDone }
            binding.tvProgress.text = "$done de ${tasks.size} completadas"
        }

        binding.fab.setOnClickListener { showAddTaskDialog() }
    }

    private fun showAddTaskDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_task, null)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nueva tarea")
            .setView(view)
            .setPositiveButton("Agregar") { _, _ ->
                val name = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_name).text.toString()
                val date = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_date).text.toString()
                val catGroup = view.findViewById<ChipGroup>(R.id.chip_group_cat)
                val prioGroup = view.findViewById<ChipGroup>(R.id.chip_group_prio)
                val cat = when (catGroup.checkedChipId) {
                    R.id.chip_personal -> "Personal"
                    R.id.chip_salud -> "Salud"
                    else -> "Trabajo"
                }
                val prio = when (prioGroup.checkedChipId) {
                    R.id.chip_alta -> "alta"
                    R.id.chip_baja -> "baja"
                    else -> "media"
                }
                if (name.isNotBlank()) {
                    viewModel.insert(Task(name = name, category = cat, dueDate = date.ifBlank { "Hoy" }, priority = prio))
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
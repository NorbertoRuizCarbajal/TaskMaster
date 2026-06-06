package com.taskmaster.home.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.taskmaster.core.task.Task
import com.taskmaster.databinding.FragmentTaskDetailBinding
import com.taskmaster.home.TaskViewModel

class TaskDetailFragment : Fragment() {
    private var _binding: FragmentTaskDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var task: Task

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        task = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireArguments().getParcelable("task", Task::class.java)
        } else {
            @Suppress("DEPRECATION")
            requireArguments().getParcelable("task")
        } ?: error("TaskDetailFragment requiere argumento 'task'")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindTaskInfo()
        setupListeners()
    }

    private fun bindTaskInfo() {
        binding.tvTaskName.text = task.name
        binding.tvCategory.text = task.category
        binding.tvDate.text = task.dueDate
        binding.tvPriority.text = task.priority.replaceFirstChar { it.uppercase() }
        binding.tvStatus.text = if (task.isDone) "✅ Completada" else "⏳ Pendiente"
        binding.checkboxDone.isChecked = task.isDone

        val color = when (task.priority.lowercase()) {
            "alta"  -> 0xFFE53935.toInt()
            "media" -> 0xFFFB8C00.toInt()
            else    -> 0xFF43A047.toInt()
        }
        binding.tvPriority.setTextColor(color)

        binding.tvPriorityBadge.text = when (task.priority.lowercase()) {
            "alta"  -> "🔴 Alta prioridad"
            "media" -> "🟡 Media prioridad"
            else    -> "🟢 Baja prioridad"
        }
        binding.tvReminderStatus.text =
            if (task.reminderId != null) "🔔 Recordatorio activo"
            else "🔕 Sin recordatorio"
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().navigateUp() }

        binding.checkboxDone.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != task.isDone) {
                viewModel.toggleDone(task)
                task = task.copy(isDone = isChecked)
                binding.tvStatus.text = if (isChecked) "✅ Completada" else "⏳ Pendiente"
                Toast.makeText(
                    requireContext(),
                    if (isChecked) "Tarea completada 🎉" else "Marcada como pendiente",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnScheduleReminder.setOnClickListener {
            viewModel.scheduleReminder(task, delayMinutes = 30)
            binding.tvReminderStatus.text = "🔔 Recordatorio en 30 minutos"
            Toast.makeText(requireContext(), "Recordatorio programado", Toast.LENGTH_SHORT).show()
        }

        binding.btnCancelReminder.setOnClickListener {
            viewModel.cancelReminder(task)
            binding.tvReminderStatus.text = "🔕 Sin recordatorio"
            Toast.makeText(requireContext(), "Recordatorio cancelado", Toast.LENGTH_SHORT).show()
        }

        binding.btnDeleteTask.setOnClickListener {
            viewModel.delete(task)
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
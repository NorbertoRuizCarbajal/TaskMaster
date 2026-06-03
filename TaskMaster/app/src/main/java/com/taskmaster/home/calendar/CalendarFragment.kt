package com.taskmaster.home.calendar

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.taskmaster.R
import com.taskmaster.core.task.Task
import com.taskmaster.databinding.FragmentCalendarBinding
import com.taskmaster.home.TaskUiState
import com.taskmaster.home.TaskViewModel
import com.taskmaster.home.tasks.TaskAdapter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var taskAdapter: TaskAdapter
    private var selectedDate: LocalDate = LocalDate.now()
    private var allTasks: List<Task> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTaskList()
        setupCalendarGrid()
        observeTasks()
    }

    private fun setupTaskList() {
        taskAdapter = TaskAdapter(
            onToggle = { viewModel.toggleDone(it) },
            onDelete = { viewModel.delete(it) }
        )
        binding.rvDayTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDayTasks.adapter = taskAdapter
    }

    private fun setupCalendarGrid() {
        updateMonthTitle()
        renderCalendar()
        binding.btnPrevMonth.setOnClickListener {
            selectedDate = selectedDate.minusMonths(1).withDayOfMonth(1)
            updateMonthTitle()
            renderCalendar()
        }
        binding.btnNextMonth.setOnClickListener {
            selectedDate = selectedDate.plusMonths(1).withDayOfMonth(1)
            updateMonthTitle()
            renderCalendar()
        }
    }

    private fun updateMonthTitle() {
        val fmt = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es"))
        binding.tvMonthYear.text = selectedDate.format(fmt).replaceFirstChar { it.uppercase() }
    }

    private fun renderCalendar() {
        binding.calendarGrid.removeAllViews()
        val firstDay = selectedDate.withDayOfMonth(1)
        val daysInMonth = selectedDate.lengthOfMonth()
        val startDow = firstDay.dayOfWeek.value % 7
        val datesWithTasks = allTasks.map { it.dueDate }.toSet()

        repeat(startDow) { binding.calendarGrid.addView(makeCell("", null)) }

        for (day in 1..daysInMonth) {
            val date = selectedDate.withDayOfMonth(day)
            val dateStr = date.format(DateTimeFormatter.ofPattern("d/M/yyyy"))
            val hasTasks = datesWithTasks.contains(dateStr) ||
                    (date == LocalDate.now() && datesWithTasks.contains("Hoy"))
            binding.calendarGrid.addView(makeCell(day.toString(), date, hasTasks))
        }
        filterTasksByDate()
    }

    private fun makeCell(
        label: String,
        date: LocalDate?,
        hasTasks: Boolean = false
    ): View {
        val cell = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_calendar_day, binding.calendarGrid, false)
        val tvDay = cell.findViewById<TextView>(R.id.tvDay)
        val dot = cell.findViewById<View>(R.id.viewDot)
        tvDay.text = label

        when {
            date == null -> tvDay.setTextColor(Color.TRANSPARENT)
            date == LocalDate.now() -> tvDay.setTextColor(Color.parseColor("#6C63FF"))
            else -> tvDay.setTextColor(Color.WHITE)
        }

        if (date != null && date == selectedDate) {
            tvDay.setBackgroundResource(R.drawable.bg_day_selected)
            tvDay.setTextColor(Color.WHITE)
        }

        dot.visibility = if (hasTasks) View.VISIBLE else View.INVISIBLE

        if (date != null) {
            cell.setOnClickListener {
                selectedDate = date
                renderCalendar()
            }
        }
        return cell
    }

    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (state is TaskUiState.Success) {
                        allTasks = state.tasks
                        renderCalendar()
                    }
                }
            }
        }
    }

    private fun filterTasksByDate() {
        val dateStr = selectedDate.format(DateTimeFormatter.ofPattern("d/M/yyyy"))
        val filtered = allTasks.filter { task ->
            task.dueDate == dateStr ||
                    (selectedDate == LocalDate.now() && task.dueDate == "Hoy")
        }
        taskAdapter.submitList(filtered)
        val fmt = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es"))
        binding.tvSelectedDate.text = if (selectedDate == LocalDate.now())
            "Tareas de hoy"
        else
            "Tareas del ${selectedDate.format(fmt)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
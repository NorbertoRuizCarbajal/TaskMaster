package com.taskmaster.home.calendar

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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarFragment : Fragment() {
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var taskAdapter: TaskAdapter
    private var selectedDate: LocalDate = LocalDate.now(ZoneId.systemDefault())
    private var allTasks: List<Task> = emptyList()

    private val ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd")

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
        setupCalendarControls()
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

    private fun setupCalendarControls() {
        updateMonthTitle()
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
        binding.tvMonthYear.text =
            selectedDate.format(fmt).replaceFirstChar { it.uppercase() }
    }

    private fun observeTasks() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    allTasks = when (state) {
                        is TaskUiState.Success -> state.tasks
                        else -> emptyList()
                    }
                    renderCalendar()
                }
            }
        }
    }

    private fun renderCalendar() {
        binding.calendarGrid.removeAllViews()
        binding.calendarGrid.columnCount = 7

        val firstDayOfMonth = selectedDate.withDayOfMonth(1)
        val daysInMonth = selectedDate.lengthOfMonth()
        val datesWithTasks = allTasks.map { it.dueDate }.toSet()

        // MON=1..SUN=7 → DOM=0, LUN=1 .. SÁB=6
        val startDow = firstDayOfMonth.dayOfWeek.value % 7

        // 42 celdas fijas (6 filas × 7 columnas)
        for (cellIndex in 0 until 42) {
            val dayNumber = cellIndex - startDow + 1
            if (dayNumber < 1 || dayNumber > daysInMonth) {
                binding.calendarGrid.addView(makeEmptyCell())
            } else {
                val date = firstDayOfMonth.withDayOfMonth(dayNumber)
                val dateStr = date.format(ISO_FORMAT)
                binding.calendarGrid.addView(
                    makeDayCell(
                        label = dayNumber.toString(),
                        date = date,
                        hasTasks = datesWithTasks.contains(dateStr)
                    )
                )
            }
        }
        updateDayTaskList()
    }

    private fun makeEmptyCell(): View {
        val cell = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_calendar_day, binding.calendarGrid, false)
        cell.visibility = View.INVISIBLE
        cell.layoutParams = android.widget.GridLayout.LayoutParams().apply {
            width = 0
            height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = android.widget.GridLayout.spec(
                android.widget.GridLayout.UNDEFINED, 1f
            )
        }
        return cell
    }

    private fun makeDayCell(label: String, date: LocalDate, hasTasks: Boolean): View {
        val cell = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_calendar_day, binding.calendarGrid, false)
        val tvDay = cell.findViewById<TextView>(R.id.tvDay)
        val dot = cell.findViewById<View>(R.id.viewDot)

        tvDay.text = label

        val today = LocalDate.now(ZoneId.systemDefault())
        when {
            date == today -> {
                tvDay.setBackgroundResource(R.drawable.bg_day_selected)
                tvDay.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )
            }
            date == selectedDate && date != today -> {
                tvDay.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.accent_purple)
                )
            }
            else -> {
                tvDay.setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                )
            }
        }

        dot.visibility = if (hasTasks) View.VISIBLE else View.INVISIBLE

        cell.layoutParams = android.widget.GridLayout.LayoutParams().apply {
            width = 0
            height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
            columnSpec = android.widget.GridLayout.spec(
                android.widget.GridLayout.UNDEFINED, 1f
            )
        }

        cell.setOnClickListener {
            selectedDate = date
            renderCalendar()
        }
        return cell
    }

    private fun updateDayTaskList() {
        val selectedDateStr = selectedDate.format(ISO_FORMAT)
        val dayTasks = allTasks.filter { it.dueDate == selectedDateStr }
        taskAdapter.submitList(dayTasks)

        val fmt = DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale("es"))
        binding.tvSelectedDate.text = if (dayTasks.isEmpty())
            "Sin tareas el ${selectedDate.format(fmt)}"
        else
            "${dayTasks.size} tarea(s) — ${selectedDate.format(fmt)}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
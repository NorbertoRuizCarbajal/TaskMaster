package com.taskmaster.home.tasks

import android.app.DatePickerDialog
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.taskmaster.R
import com.taskmaster.core.task.Task
import com.taskmaster.databinding.DialogAddTaskBinding
import com.taskmaster.databinding.FragmentHomeBinding
import com.taskmaster.home.TaskUiState
import com.taskmaster.home.TaskViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels()
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeToDelete()
        observeUiState()
        observeNavigation()
        binding.fab.setOnClickListener { showAddTaskDialog() }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onToggle = { viewModel.toggleDone(it) },
            onDelete = { viewModel.delete(it) },
            onItemClick = { viewModel.onTaskClicked(it) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is TaskUiState.Loading -> {
                            binding.recyclerView.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.GONE
                        }
                        is TaskUiState.Success -> {
                            val tasks = state.tasks
                            adapter.submitList(tasks)
                            val done = tasks.count { it.isDone }
                            binding.tvProgress.text = "$done de ${tasks.size} completadas"
                            binding.recyclerView.visibility = View.VISIBLE
                            binding.layoutEmpty.visibility = View.GONE
                        }
                        is TaskUiState.Empty -> {
                            adapter.submitList(emptyList())
                            binding.tvProgress.text = "0 de 0 completadas"
                            binding.recyclerView.visibility = View.GONE
                            binding.layoutEmpty.visibility = View.VISIBLE
                        }
                        is TaskUiState.Error -> {
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun observeNavigation() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.navigateToDetail.collect { task ->
                    val bundle = Bundle().apply { putParcelable("task", task) }
                    findNavController().navigate(
                        R.id.action_homeFragment_to_taskDetailFragment, bundle
                    )
                }
            }
        }
    }

    private fun setupSwipeToDelete() {
        val deleteColor = ContextCompat.getColor(requireContext(), R.color.swipe_delete_red)
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        val paint = Paint().apply { color = deleteColor }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_ID.toInt()) return
                val task = adapter.currentList[position]
                viewModel.delete(task)
                Snackbar.make(binding.root, "\"${task.name}\" eliminada", Snackbar.LENGTH_LONG)
                    .setAction("DESHACER") {
                        viewModel.insert(task.copy(id = 0))
                    }.show()
            }

            override fun onChildDraw(
                c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = vh.itemView
                    val iconSize = (24 * resources.displayMetrics.density).toInt()
                    c.drawRoundRect(
                        RectF(
                            itemView.right + dX, itemView.top.toFloat(),
                            itemView.right.toFloat(), itemView.bottom.toFloat()
                        ), 16f, 16f, paint
                    )
                    deleteIcon?.let {
                        val iconTop = itemView.top + (itemView.height - iconSize) / 2
                        val iconLeft = itemView.right - iconSize -
                                (24 * resources.displayMetrics.density).toInt()
                        it.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
                        it.setTint(android.graphics.Color.WHITE)
                        it.draw(c)
                    }
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive)
            }
        }).attachToRecyclerView(binding.recyclerView)
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(layoutInflater)

        dialogBinding.etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    dialogBinding.etDate.setText(
                        "%04d-%02d-%02d".format(year, month + 1, day)
                    )
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nueva Tarea")
            .setView(dialogBinding.root)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Guardar") { _, _ ->
                val name = dialogBinding.etName.text.toString().trim()
                if (name.isBlank()) {
                    Snackbar.make(
                        binding.root, "El nombre es requerido", Snackbar.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }
                val category = when (dialogBinding.chipGroupCat.checkedChipId) {
                    dialogBinding.chipTrabajo.id  -> "Trabajo"
                    dialogBinding.chipPersonal.id -> "Personal"
                    dialogBinding.chipSalud.id    -> "Salud"
                    else -> "General"
                }
                val priority = when (dialogBinding.chipGroupPrio.checkedChipId) {
                    dialogBinding.chipAlta.id  -> "alta"
                    dialogBinding.chipMedia.id -> "media"
                    else -> "baja"
                }
                val dueDate = dialogBinding.etDate.text.toString().trim().ifBlank {
                    LocalDate.now(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                }
                val task = Task(
                    name = name,
                    category = category,
                    dueDate = dueDate,
                    priority = priority,
                    priorityLevel = Task.priorityToLevel(priority)
                )
                viewModel.insert(task)
                viewModel.scheduleReminder(task, delayMinutes = 60)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
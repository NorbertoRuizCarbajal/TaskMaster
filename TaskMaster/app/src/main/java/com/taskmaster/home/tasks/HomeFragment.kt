package com.taskmaster.home.tasks

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.taskmaster.R
import com.taskmaster.core.task.Task
import com.taskmaster.databinding.FragmentHomeBinding
import com.taskmaster.home.TaskUiState
import com.taskmaster.home.TaskViewModel
import kotlinx.coroutines.launch

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
        binding.fab.setOnClickListener { showAddTaskDialog() }
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onToggle = { viewModel.toggleDone(it) },
            onDelete = { viewModel.delete(it) }
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

    private fun setupSwipeToDelete() {
        val deleteColor = ContextCompat.getColor(requireContext(), R.color.swipe_delete_red)
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        val paint = Paint().apply { color = deleteColor }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                t: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // BUG CORREGIDO: adapterPosition puede ser -1 si la lista cambió
                // durante el gesto — sin este guard crashea con IndexOutOfBounds.
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_ID.toInt()) return
                val task = adapter.currentList[position]
                viewModel.delete(task)
                Snackbar.make(binding.root, "\"${task.name}\" eliminada", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer") { viewModel.insert(task) }
                    .setBackgroundTint(ContextCompat.getColor(requireContext(), R.color.card_dark))
                    .setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                    .setActionTextColor(ContextCompat.getColor(requireContext(), R.color.purple_primary))
                    .show()
            }

            override fun onChildDraw(
                canvas: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val item = viewHolder.itemView
                if (dX < 0) {
                    val bg = RectF(item.right + dX, item.top + 4f,
                        item.right.toFloat(), item.bottom - 4f)
                    canvas.drawRoundRect(bg, 32f, 32f, paint)
                    deleteIcon?.let {
                        val size = 48; val margin = 32
                        val top = item.top + (item.height - size) / 2
                        val left = item.right - margin - size
                        if (item.right + dX < left.toFloat()) {
                            it.setBounds(left, top, left + size, top + size)
                            it.draw(canvas)
                        }
                    }
                }
                super.onChildDraw(canvas, recyclerView, viewHolder,
                    dX, dY, actionState, isCurrentlyActive)
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.4f
        }).attachToRecyclerView(binding.recyclerView)
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nueva tarea")
            .setView(dialogView)
            .setPositiveButton("Agregar") { _, _ ->
                val name = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
                    R.id.etName).text.toString().trim()
                val date = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(
                    R.id.etDate).text.toString().trim()
                val catGroup = dialogView.findViewById<ChipGroup>(R.id.chipGroupCat)
                val prioGroup = dialogView.findViewById<ChipGroup>(R.id.chipGroupPrio)
                val cat = when (catGroup.checkedChipId) {
                    R.id.chipPersonal -> "Personal"
                    R.id.chipSalud   -> "Salud"
                    else             -> "Trabajo"
                }
                val prio = when (prioGroup.checkedChipId) {
                    R.id.chipAlta -> "alta"
                    R.id.chipBaja -> "baja"
                    else          -> "media"
                }

                if (name.isNotBlank()) {
                    viewModel.insert(Task(
                        name = name, category = cat,
                        dueDate = date.ifBlank { "Hoy" }, priority = prio
                    ))
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
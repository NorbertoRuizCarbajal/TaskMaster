package com.taskmaster.ui.home

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.taskmaster.R
import com.taskmaster.data.Task
import com.taskmaster.databinding.FragmentHomeBinding
import com.taskmaster.viewmodel.TaskViewModel

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TaskViewModel
    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        // Configurar swipe para eliminar
        setupSwipeToDelete()

        viewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            val done = tasks.count { it.isDone }
            binding.tvProgress.text = "$done de ${tasks.size} completadas"

            // Mostrar empty state si no hay tareas
            if (tasks.isEmpty()) {
                binding.layoutEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.layoutEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        binding.fab.setOnClickListener { showAddTaskDialog() }
    }

    private fun setupSwipeToDelete() {
        val deleteColor = ContextCompat.getColor(requireContext(), R.color.swipe_delete_red)
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_swipe_delete)
        val paint = Paint().apply { color = deleteColor }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, // sin drag
            ItemTouchHelper.LEFT // solo swipe izquierda
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = adapter.currentList[position]

                // Eliminar tarea
                viewModel.delete(task)

                // Snackbar con opción de deshacer
                Snackbar.make(
                    binding.root,
                    "\"${task.name}\" eliminada",
                    Snackbar.LENGTH_LONG
                ).setAction("Deshacer") {
                    viewModel.insert(task)
                }.setBackgroundTint(
                    ContextCompat.getColor(requireContext(), R.color.snackbar_bg)
                ).setTextColor(
                    ContextCompat.getColor(requireContext(), android.R.color.white)
                ).setActionTextColor(
                    ContextCompat.getColor(requireContext(), R.color.purple_primary)
                ).show()
            }

            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val cornerRadius = 32f

                if (dX < 0) { // swipe izquierda
                    // Fondo rojo con esquinas redondeadas
                    val background = RectF(
                        itemView.right + dX,
                        itemView.top.toFloat() + 4,
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat() - 4
                    )
                    canvas.drawRoundRect(background, cornerRadius, cornerRadius, paint)

                    // Ícono de basura centrado en el área roja
                    deleteIcon?.let { icon ->
                        val iconSize = 48
                        val iconMargin = 32
                        val iconTop = itemView.top + (itemView.height - iconSize) / 2
                        val iconLeft = itemView.right - iconMargin - iconSize
                        val iconRight = itemView.right - iconMargin
                        val iconBottom = iconTop + iconSize

                        // Solo mostrar ícono si hay suficiente espacio
                        if (itemView.right + dX < iconLeft.toFloat()) {
                            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                            icon.draw(canvas)
                        }
                    }
                }

                super.onChildDraw(
                    canvas, recyclerView, viewHolder,
                    dX, dY, actionState, isCurrentlyActive
                )
            }

            // Ajustar velocidad del swipe
            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = 0.4f
            override fun getSwipeVelocityThreshold(defaultValue: Float) = defaultValue * 0.7f
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun showAddTaskDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_add_task, null)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nueva tarea")
            .setView(view)
            .setPositiveButton("Agregar") { _, _ ->
                val name = view.findViewById<com.google.android.material.textfield.TextInputEditText>(
                    R.id.et_name).text.toString()
                val date = view.findViewById<com.google.android.material.textfield.TextInputEditText>(
                    R.id.et_date).text.toString()
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
                    viewModel.insert(
                        Task(
                            name = name,
                            category = cat,
                            dueDate = date.ifBlank { "Hoy" },
                            priority = prio
                        )
                    )
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
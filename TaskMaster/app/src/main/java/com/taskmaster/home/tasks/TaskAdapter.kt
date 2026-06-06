package com.taskmaster.home.tasks

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.taskmaster.R
import com.taskmaster.core.task.Task

class TaskAdapter(
    private val onToggle: (Task) -> Unit,
    private val onDelete: (Task) -> Unit,
    private val onItemClick: (Task) -> Unit = {}
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(DiffCallback()) {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: MaterialCheckBox = view.findViewById(R.id.checkbox)
        val tvName: TextView = view.findViewById(R.id.tvTaskName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvPriority: TextView = view.findViewById(R.id.tvPriority)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder =
        TaskViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        )

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.tvName.text = task.name
        holder.tvCategory.text = task.category
        holder.tvDate.text = task.dueDate
        holder.tvPriority.text = task.priority.replaceFirstChar { it.uppercase() }
        holder.checkbox.isChecked = task.isDone

        holder.tvName.paintFlags = if (task.isDone)
            holder.tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else
            holder.tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        val color = when (task.priority.lowercase()) {
            "alta"  -> 0xFFE53935.toInt()
            "media" -> 0xFFFB8C00.toInt()
            else    -> 0xFF43A047.toInt()
        }
        holder.tvPriority.setTextColor(color)

        holder.checkbox.setOnClickListener { onToggle(task) }
        holder.btnDelete.setOnClickListener { onDelete(task) }
        holder.itemView.setOnClickListener { onItemClick(task) }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(a: Task, b: Task) = a.id == b.id
        override fun areContentsTheSame(a: Task, b: Task) = a == b
    }
}
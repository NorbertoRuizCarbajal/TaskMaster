package com.taskmaster.home.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.taskmaster.R
import com.taskmaster.databinding.FragmentStatsBinding
import com.taskmaster.home.TaskViewModel

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TaskViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        setupChart()
        observeData()
    }

    private fun setupChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)
            legend.isEnabled = false
            setTouchEnabled(false)
            animateY(800)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.parseColor("#9E9EC8")
                gridColor = Color.parseColor("#3D3A6B")
                axisLineColor = Color.parseColor("#3D3A6B")
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.apply {
                textColor = Color.parseColor("#9E9EC8")
                gridColor = Color.parseColor("#3D3A6B")
                axisLineColor = Color.parseColor("#3D3A6B")
                granularity = 1f
                axisMinimum = 0f
            }
            axisRight.isEnabled = false
        }
    }

    private fun observeData() {
        viewModel.total.observe(viewLifecycleOwner) { binding.tvTotal.text = it.toString() }

        viewModel.totalCompleted.observe(viewLifecycleOwner) { completed ->
            binding.tvCompleted.text = completed.toString()
            val total = viewModel.total.value ?: 0
            val pct = if (total > 0) (completed * 100 / total) else 0
            binding.tvProductivityPct.text = "$pct%"
            binding.progressWeekly.progress = pct
            binding.tvProductivityLabel.text = when {
                pct >= 80 -> "¡Excelente productividad! 🎯"
                pct >= 50 -> "Buen ritmo, sigue así 💪"
                pct > 0   -> "de tus tareas están completadas"
                else      -> "Aún no has completado tareas"
            }
        }

        viewModel.statsByDate.observe(viewLifecycleOwner) { stats ->
            if (stats.isEmpty()) return@observe
            val recent = stats.takeLast(7)
            val entries = recent.mapIndexed { i, s -> BarEntry(i.toFloat(), s.completed.toFloat()) }
            val labels = recent.map { it.dueDate }
            val dataSet = BarDataSet(entries, "").apply {
                color = Color.parseColor("#6C63FF")
                valueTextColor = Color.parseColor("#9E9EC8")
                valueTextSize = 10f
            }
            binding.barChart.data = BarData(dataSet).apply { barWidth = 0.6f }
            binding.barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            binding.barChart.xAxis.labelCount = labels.size
            binding.barChart.invalidate()
        }

        viewModel.statsByCategory.observe(viewLifecycleOwner) { stats ->
            binding.layoutCategories.removeAllViews()
            stats.forEach { stat ->
                val container = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(0, 0, 0, 24)
                }
                val header = LinearLayout(requireContext()).apply { orientation = LinearLayout.HORIZONTAL }
                val tvCat = TextView(requireContext()).apply {
                    text = stat.category; textSize = 13f; setTextColor(Color.WHITE)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                }
                val tvCount = TextView(requireContext()).apply {
                    text = "${stat.completed}/${stat.total}"; textSize = 13f
                    setTextColor(Color.parseColor("#9E9EC8"))
                }
                header.addView(tvCat); header.addView(tvCount)
                val bar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
                    max = stat.total; progress = stat.completed
                    progressDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.progress_bar_custom)
                    layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 20).apply { topMargin = 8 }
                }
                container.addView(header); container.addView(bar)
                binding.layoutCategories.addView(container)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

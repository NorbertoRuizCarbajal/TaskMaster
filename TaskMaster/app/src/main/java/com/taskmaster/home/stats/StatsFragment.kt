package com.taskmaster.home.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.taskmaster.databinding.FragmentStatsBinding
import com.taskmaster.home.TaskViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        observeStats()
    }

    private fun setupChart() {
        binding.barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setBackgroundColor(Color.TRANSPARENT)
            legend.isEnabled = false
            setTouchEnabled(false)
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

    private fun observeStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // combine evita división por cero al esperar ambos valores
                launch {
                    combine(
                        viewModel.total,
                        viewModel.totalCompleted
                    ) { total, completed -> Pair(total, completed) }
                        .collect { (total, completed) ->
                            binding.tvTotal.text = total.toString()
                            binding.tvCompleted.text = completed.toString()
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
                }

                launch {
                    viewModel.statsByDate.collect { stats ->
                        if (stats.isEmpty()) {
                            binding.barChart.clear()
                            binding.barChart.invalidate()
                            return@collect
                        }
                        val recent = stats.takeLast(7)
                        val labels = recent.map { it.dueDate.takeLast(5) }
                        val entries = recent.mapIndexed { i, s ->
                            BarEntry(i.toFloat(), s.completed.toFloat())
                        }
                        val dataSet = BarDataSet(entries, "Completadas").apply {
                            color = Color.parseColor("#6C63FF")
                            valueTextColor = Color.parseColor("#9E9EC8")
                            valueTextSize = 10f
                        }
                        binding.barChart.apply {
                            data = BarData(dataSet)
                            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                            animateY(600)
                            invalidate()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
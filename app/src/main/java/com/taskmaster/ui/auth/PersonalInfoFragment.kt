package com.taskmaster.ui.auth

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taskmaster.R
import com.taskmaster.databinding.FragmentPersonalInfoBinding
import java.util.Calendar

class PersonalInfoFragment : Fragment() {
    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etFecha.setOnClickListener { showDatePicker() }
        binding.tilFecha.setEndIconOnClickListener { showDatePicker() }

        binding.btnFinish.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val apellidos = binding.etApellidos.text.toString()
            val username = binding.etUsername.text.toString()
            val telefono = binding.etTelefono.text.toString()
            var valid = true

            if (nombre.isBlank()) { binding.tilNombre.error = "Campo requerido"; valid = false }
            if (apellidos.isBlank()) { binding.tilApellidos.error = "Campo requerido"; valid = false }
            if (username.isBlank()) { binding.tilUsername.error = "Campo requerido"; valid = false }
            if (telefono.isBlank()) { binding.tilTelefono.error = "Campo requerido"; valid = false }

            if (valid) findNavController().navigate(R.id.action_personalInfo_to_home)
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            binding.etFecha.setText("$day/${month + 1}/$year")
        }, cal.get(Calendar.YEAR) - 18, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
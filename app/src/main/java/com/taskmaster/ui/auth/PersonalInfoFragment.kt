package com.taskmaster.ui.auth

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taskmaster.R
import com.taskmaster.SessionManager
import com.taskmaster.databinding.FragmentPersonalInfoBinding
import java.util.Calendar

class PersonalInfoFragment : Fragment() {
    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        binding.etFecha.setOnClickListener { showDatePicker() }
        binding.tilFecha.setEndIconOnClickListener { showDatePicker() }

        binding.btnFinish.setOnClickListener {
            val nombre = binding.etNombre.text.toString().trim()
            val apellidos = binding.etApellidos.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()
            val fecha = binding.etFecha.text.toString().trim()
            var valid = true

            if (nombre.isBlank()) {
                binding.tilNombre.error = "Campo requerido"; valid = false
            } else if (nombre.length < 2) {
                binding.tilNombre.error = "Nombre muy corto"; valid = false
            } else binding.tilNombre.error = null

            if (apellidos.isBlank()) {
                binding.tilApellidos.error = "Campo requerido"; valid = false
            } else binding.tilApellidos.error = null

            if (username.isBlank()) {
                binding.tilUsername.error = "Campo requerido"; valid = false
            } else if (username.length < 3) {
                binding.tilUsername.error = "Mínimo 3 caracteres"; valid = false
            } else if (!username.matches(Regex("^[a-zA-Z0-9._]+$"))) {
                binding.tilUsername.error = "Solo letras, números, puntos y guiones bajos"; valid = false
            } else binding.tilUsername.error = null

            if (telefono.isBlank()) {
                binding.tilTelefono.error = "Campo requerido"; valid = false
            } else if (telefono.length < 10) {
                binding.tilTelefono.error = "Teléfono inválido"; valid = false
            } else binding.tilTelefono.error = null

            if (valid) {
                sessionManager.savePersonalInfo(nombre, apellidos, username, telefono, fecha)
                findNavController().navigate(R.id.action_personalInfo_to_home)
            }
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.etFecha.setText("$day/${month + 1}/$year")
            },
            cal.get(Calendar.YEAR) - 18,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
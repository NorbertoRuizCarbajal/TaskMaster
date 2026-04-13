package com.taskmaster.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taskmaster.R
import com.taskmaster.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupValidation()

        binding.btnRegister.setOnClickListener { validateAndContinue() }
    }

    private fun setupValidation() {
        // Validar email en tiempo real
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                when {
                    email.isBlank() -> binding.tilEmail.error = null
                    !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                        binding.tilEmail.error = "Correo no válido"
                    else -> {
                        binding.tilEmail.error = null
                        binding.tilEmail.isErrorEnabled = false
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Validar contraseña en tiempo real
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pass = s.toString()
                when {
                    pass.isBlank() -> binding.tilPassword.error = null
                    pass.length < 8 -> binding.tilPassword.error = "Mínimo 8 caracteres"
                    !pass.any { it.isDigit() } -> binding.tilPassword.error = "Debe incluir al menos un número"
                    !pass.any { it.isUpperCase() } -> binding.tilPassword.error = "Debe incluir una mayúscula"
                    else -> {
                        binding.tilPassword.error = null
                        binding.tilPassword.isErrorEnabled = false
                    }
                }
                // Re-validar confirmación si ya tiene texto
                val confirm = binding.etConfirmPassword.text.toString()
                if (confirm.isNotBlank() && confirm != pass) {
                    binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                } else if (confirm.isNotBlank()) {
                    binding.tilConfirmPassword.error = null
                    binding.tilConfirmPassword.isErrorEnabled = false
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Validar confirmación en tiempo real
        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val confirm = s.toString()
                val pass = binding.etPassword.text.toString()
                when {
                    confirm.isBlank() -> binding.tilConfirmPassword.error = null
                    confirm != pass -> binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                    else -> {
                        binding.tilConfirmPassword.error = null
                        binding.tilConfirmPassword.isErrorEnabled = false
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validateAndContinue() {
        val email = binding.etEmail.text.toString().trim()
        val pass = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()
        var valid = true

        if (email.isBlank()) {
            binding.tilEmail.error = "Campo requerido"
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Correo no válido"
            valid = false
        }

        if (pass.isBlank()) {
            binding.tilPassword.error = "Campo requerido"
            valid = false
        } else if (pass.length < 8) {
            binding.tilPassword.error = "Mínimo 8 caracteres"
            valid = false
        } else if (!pass.any { it.isDigit() }) {
            binding.tilPassword.error = "Debe incluir al menos un número"
            valid = false
        } else if (!pass.any { it.isUpperCase() }) {
            binding.tilPassword.error = "Debe incluir una mayúscula"
            valid = false
        }

        if (confirm.isBlank()) {
            binding.tilConfirmPassword.error = "Campo requerido"
            valid = false
        } else if (confirm != pass) {
            binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
            valid = false
        }

        if (valid) findNavController().navigate(R.id.action_register_to_personalInfo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
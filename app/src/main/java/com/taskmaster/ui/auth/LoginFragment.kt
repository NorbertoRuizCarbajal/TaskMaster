package com.taskmaster.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taskmaster.R
import com.taskmaster.SessionManager
import com.taskmaster.core.FragmentCommunicator
import com.taskmaster.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private var communicator: FragmentCommunicator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        // Obtener el communicator desde la Activity
        communicator = activity as? FragmentCommunicator

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            var valid = true

            // Validaciones
            if (email.isBlank()) {
                binding.tilEmail.error = "Ingresa tu correo"
                valid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Correo no válido"
                valid = false
            } else {
                binding.tilEmail.error = null
            }

            if (password.isBlank()) {
                binding.tilPassword.error = "Ingresa tu contraseña"
                valid = false
            } else if (password.length < 6) {
                binding.tilPassword.error = "Mínimo 6 caracteres"
                valid = false
            } else {
                binding.tilPassword.error = null
            }

            if (valid) {
                showLoaderAndLogin(email)
            }
        }

        binding.tvForgot.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.etEmail.setOnFocusChangeListener { _, _ -> binding.tilEmail.error = null }
        binding.etPassword.setOnFocusChangeListener { _, _ -> binding.tilPassword.error = null }
    }

    private fun showLoaderAndLogin(email: String) {
        // Mostrar loader
        communicator?.manageLoader(true)

        // Simular delay de red (1.5 segundos) y luego navegar
        binding.root.postDelayed({
            // Ocultar loader
            communicator?.manageLoader(false)

            // Guardar sesión y navegar
            sessionManager.saveLoginSession(email)
            if (isAdded) {
                findNavController().navigate(R.id.action_login_to_home)
            }
        }, 3000)
    }

    override fun onDestroyView() {
        communicator?.manageLoader(false)
        super.onDestroyView()
        _binding = null
    }
}
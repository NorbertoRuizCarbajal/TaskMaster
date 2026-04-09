package com.taskmaster.ui.auth

import android.os.Bundle
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

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()
            val confirm = binding.etConfirmPassword.text.toString()
            var valid = true

            if (email.isBlank()) { binding.tilEmail.error = "Campo requerido"; valid = false }
            if (pass.isBlank()) { binding.tilPassword.error = "Campo requerido"; valid = false }
            if (pass != confirm) { binding.tilConfirmPassword.error = "Las contraseñas no coinciden"; valid = false }

            if (valid) findNavController().navigate(R.id.action_register_to_personalInfo)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
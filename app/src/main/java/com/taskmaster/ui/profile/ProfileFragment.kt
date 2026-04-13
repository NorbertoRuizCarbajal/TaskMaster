package com.taskmaster.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taskmaster.R
import com.taskmaster.SessionManager
import com.taskmaster.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Mostrar datos guardados
        binding.tvName.text = sessionManager.getUserName().ifBlank { "Usuario" }
        binding.tvEmail.text = sessionManager.getUserEmail()
        binding.tvUsername.text = "@${sessionManager.getUsername().ifBlank { "usuario" }}"
        binding.tvPhone.text = sessionManager.getPhone().ifBlank { "No registrado" }
        binding.tvBirthdate.text = sessionManager.getBirthdate().ifBlank { "No registrado" }

        // Iniciales para el avatar
        val name = sessionManager.getUserName()
        val initials = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
        binding.tvAvatar.text = initials.ifBlank { "U" }

        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            val navOptions = androidx.navigation.navOptions {
                popUpTo(0) { inclusive = true }
            }
            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
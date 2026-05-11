package com.taskmaster.home.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.taskmaster.core.ResponseService
import com.taskmaster.core.repositories.UserRepository
import com.taskmaster.databinding.FragmentProfileBinding
import com.taskmaster.onboarding.MainActivity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userRepository = UserRepository()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadProfile()

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        binding.tvEmail.text = email

        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = userRepository.getUserInfo(uid)) {
                is ResponseService.Success -> {
                    val p = result.data
                    val fullName = "${p.firstName} ${p.lastName}".trim()
                    binding.tvName.text = fullName.ifBlank { "Usuario" }
                    binding.tvUsername.text = "@${p.userName.ifBlank { "usuario" }}"
                    binding.tvPhone.text = p.phone.ifBlank { "No registrado" }
                    binding.tvBirthdate.text = p.birthDate.ifBlank { "No registrado" }
                    val initials = fullName.split(" ")
                        .filter { it.isNotBlank() }
                        .take(2)
                        .joinToString("") { it.first().uppercase() }
                    binding.tvAvatar.text = initials.ifBlank { "U" }
                }
                is ResponseService.Error -> {
                    binding.tvName.text = email.substringBefore("@")
                    binding.tvAvatar.text = email.firstOrNull()?.uppercase() ?: "U"
                    Snackbar.make(binding.root, "No se pudo cargar el perfil", Snackbar.LENGTH_SHORT).show()
                }
                else -> Unit
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

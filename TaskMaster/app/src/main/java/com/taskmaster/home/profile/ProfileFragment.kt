package com.taskmaster.home.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.taskmaster.core.ResponseService
import com.taskmaster.core.repositories.UserProfile
import com.taskmaster.core.repositories.UserRepository
import com.taskmaster.databinding.FragmentProfileBinding
import com.taskmaster.onboarding.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val profile: UserProfile, val email: String) : ProfileUiState()
    data class Error(val message: String, val email: String) : ProfileUiState()
}

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val userRepository = UserRepository()

    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    private val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeProfileState()
        loadProfile()
        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun observeProfileState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileState.collect { state ->
                    when (state) {
                        is ProfileUiState.Loading -> {
                            binding.cardInfo.visibility = View.INVISIBLE
                            binding.tvAvatar.text = "..."
                        }
                        is ProfileUiState.Success -> {
                            binding.cardInfo.visibility = View.VISIBLE
                            renderProfile(state.profile, state.email)
                        }
                        is ProfileUiState.Error -> {
                            binding.cardInfo.visibility = View.VISIBLE
                            renderFallback(state.email)
                            if (state.message.isNotBlank())
                                Snackbar.make(
                                    binding.root, state.message, Snackbar.LENGTH_SHORT
                                ).show()
                        }
                    }
                }
            }
        }
    }

    private fun loadProfile() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
        if (uid == null) {
            _profileState.value = ProfileUiState.Error("Sesión no encontrada", email)
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            _profileState.value = ProfileUiState.Loading
            when (val result = userRepository.getUserInfo(uid)) {
                is ResponseService.Success ->
                    _profileState.value = ProfileUiState.Success(result.data, email)
                is ResponseService.Error ->
                    _profileState.value = ProfileUiState.Error(result.error, email)
                is ResponseService.Loading -> {}
            }
        }
    }

    private fun renderProfile(profile: UserProfile, email: String) {
        val fullName = "${profile.firstName} ${profile.lastName}".trim()
        binding.tvEmail.text = email
        binding.tvName.text = fullName.ifBlank { email.substringBefore("@") }
        binding.tvUsername.text = "@${profile.userName.ifBlank { "usuario" }}"
        binding.tvPhone.text = profile.phone.ifBlank { "No registrado" }
        binding.tvBirthdate.text = profile.birthDate.ifBlank { "No registrado" }
        val initials = fullName.split(" ")
            .filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }
        binding.tvAvatar.text = initials.ifBlank { email.firstOrNull()?.uppercase() ?: "U" }
    }

    private fun renderFallback(email: String) {
        binding.tvEmail.text = email
        binding.tvName.text = email.substringBefore("@")
        binding.tvUsername.text = "@usuario"
        binding.tvPhone.text = "No registrado"
        binding.tvBirthdate.text = "No registrado"
        binding.tvAvatar.text = email.firstOrNull()?.uppercase() ?: "U"
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
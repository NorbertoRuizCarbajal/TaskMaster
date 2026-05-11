package com.taskmaster.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.taskmaster.core.FragmentCommunicator
import com.taskmaster.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity(), FragmentCommunicator {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.navHostFragment.id) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)
    }

    override fun manageLoader(isVisible: Boolean) {
        binding.loaderContainer.isVisible = isVisible
        if (isVisible) binding.loaderLottie.playAnimation()
        else binding.loaderLottie.cancelAnimation()
    }
}

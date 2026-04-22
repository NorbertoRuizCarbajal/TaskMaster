package com.taskmaster

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.taskmaster.core.FragmentCommunicator
import com.taskmaster.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), FragmentCommunicator {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(navController)

        if (intent.getStringExtra("destination") == "home") {
            val navOptions = androidx.navigation.navOptions {
                popUpTo(R.id.loginFragment) { inclusive = true }
            }
            navController.navigate(R.id.homeFragment, null, navOptions)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.forgotPasswordFragment,
                R.id.personalInfoFragment -> binding.bottomNavigation.visibility = View.GONE
                else -> binding.bottomNavigation.visibility = View.VISIBLE
            }
        }
    }

    override fun manageLoader(isVisible: Boolean) {
        binding.loaderContainer.isVisible = isVisible
        if (isVisible) {
            binding.loaderView.playAnimation()
        } else {
            binding.loaderView.cancelAnimation()
        }
    }
}
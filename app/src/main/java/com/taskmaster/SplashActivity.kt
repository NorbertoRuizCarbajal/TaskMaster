package com.taskmaster

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.taskmaster.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sessionManager = SessionManager(this)
        animateAndNavigate()
    }

    private fun animateAndNavigate() {
        val interpolator = AccelerateDecelerateInterpolator()

        binding.ivLogo.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(600)
            .setInterpolator(interpolator)
            .start()

        binding.tvName.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(400)
            .setInterpolator(interpolator)
            .start()

        binding.tvTagline.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(700)
            .setInterpolator(interpolator)
            .withEndAction {
                navigateNext()
            }
            .start()
    }

    private fun navigateNext() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            // Si ya hay sesión, ir directo a Home
            if (sessionManager.isLoggedIn()) {
                putExtra("destination", "home")
            }
        })
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
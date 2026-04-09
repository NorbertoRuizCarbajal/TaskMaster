package com.taskmaster

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.taskmaster.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animateAndNavigate()
    }

    private fun animateAndNavigate() {
        val interpolator = AccelerateDecelerateInterpolator()

        // Logo: escala + fade in
        binding.ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .setInterpolator(interpolator)
            .start()

        // Nombre: fade in con delay
        binding.tvName.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(400)
            .setInterpolator(interpolator)
            .start()

        // Tagline: fade in con más delay
        binding.tvTagline.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(700)
            .setInterpolator(interpolator)
            .withEndAction {
                // Después de la animación, ir a MainActivity
                startActivity(Intent(this, MainActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
            .start()
    }
}
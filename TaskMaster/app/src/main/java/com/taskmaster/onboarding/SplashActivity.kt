package com.taskmaster.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.taskmaster.R
import com.taskmaster.home.HomeActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val lottie = findViewById<LottieAnimationView>(R.id.splashLottie)
        val interpolator = AccelerateDecelerateInterpolator()

        lottie.animate()
            .alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(700)
            .setInterpolator(interpolator)
            .withEndAction {
                lottie.postDelayed({ navigateNext() }, 2000)
            }.start()
    }

    private fun navigateNext() {
        val user = FirebaseAuth.getInstance().currentUser
        val intent = if (user != null) {
            Intent(this, HomeActivity::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

package com.example.tsl_app.activities.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.tsl_app.R
import com.example.tsl_app.activities.login.LoginActivity

/** A Splash Screen is typically the first screen shown when the app is launched.
 * It is usually displayed for a few seconds while the app is loading necessary resources.
 * Transition: Provides a smooth transition from the system launch to the app's main content.
 * Update By Vikas Roy in Kotlin 2/6/2025
 */

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setStatusBarColor("#7799CC")
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            val i = Intent(this@SplashActivity, LoginActivity::class.java)
            startActivity(i)
            finish()
        }, 2000)
    }

    private fun setStatusBarColor(color: String) {
        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor(color)
    }
}
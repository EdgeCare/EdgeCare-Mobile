package com.example.edgecare.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.R

@SuppressLint("CustomSplashScreen")
class SplashActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // [ToDo] - Perform any pre-loading work here if needed
        // e.g., checking user login status from SharedPreferences or database

        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is signed in
            val isLoggedIn = checkIfUserLoggedIn()

            if (isLoggedIn) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
//                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish()
        }, 1000) // Delay for 1 seconds or however long the loading takes
    }

    private fun checkIfUserLoggedIn(): Boolean {
        // [ToDO] - login logic
        return true
    }
}
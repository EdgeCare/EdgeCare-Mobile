package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.databinding.ActivityAuthBinding

class AuthActivity: AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mainIntent = Intent(this, MainActivity::class.java)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogin.setOnClickListener {
//            startActivity(Intent(this, LoginActivity::class.java))
            startActivity(mainIntent)
        }

        binding.buttonCreateAccount.setOnClickListener {
//            startActivity(Intent(this, CreateAccountActivity::class.java))
            startActivity(mainIntent)
        }
    }
}
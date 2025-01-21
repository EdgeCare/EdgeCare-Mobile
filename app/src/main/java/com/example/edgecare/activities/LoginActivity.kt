package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.api.userLogin
import com.example.edgecare.databinding.ActivitySignInBinding

class LoginActivity: AppCompatActivity()  {
    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                userLogin(email, password) { success, message ->
                    if (success) {
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT)
                            .show()
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        Toast.makeText(this, "Login failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.toSignUp.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

}

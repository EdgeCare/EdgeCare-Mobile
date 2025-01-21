package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.api.userSignUp
import com.example.edgecare.databinding.ActivitySignUpBinding

class SignupActivity: AppCompatActivity()  {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signup.setOnClickListener {
            val email = binding.signupEmail.text.toString()
            val password = binding.signupPassword.text.toString()
            val confirmPassword = binding.signupConfirmPassword.text.toString()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
            } else if ( password.length < 6 ) {
                Toast.makeText(this, "Password must have at least 6 characters", Toast.LENGTH_SHORT).show()
            } else if ( password != confirmPassword ) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                userSignUp(email, password) { success, message ->
                    if (success) {
                        Toast.makeText(this, "Sign Up successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, QuestionnaireActivity::class.java))
                    } else {
                        Toast.makeText(this, "Sign Up failed: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.toLogIn.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

}
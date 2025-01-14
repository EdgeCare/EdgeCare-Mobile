package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.databinding.ActivitySignUpBinding

class SignupActivity: AppCompatActivity()  {
    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signup.setOnClickListener {
            startActivity(Intent(this, QuestionnaireActivity::class.java))
        }

    }

}
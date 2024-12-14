package com.example.edgecare.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_bar)

        // Load MainContentActivity fragment <-- Chat Area -->
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.chatContentFrame, MainContentFragment())
                .commit()
        }
    }
}

package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.R

class TopBarActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_bar)

        // Load the MainContentActivity into the FrameLayout
        supportFragmentManager.beginTransaction()
            .replace(R.id.chatContentFrame, MainContentActivity())
            .commit()
    }
}


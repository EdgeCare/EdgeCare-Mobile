package com.example.edgecare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.objectbox.Box
import io.objectbox.BoxStore

class CollectPersonaDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_collect_persona_data)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        lateinit var boxStore: BoxStore
        lateinit var userDetailsBox: Box<Persona>

        boxStore = ObjectBox.store
        userDetailsBox = boxStore.boxFor(Persona::class.java)

        val nameEditText = findViewById<EditText>(R.id.nameEditText)
        val ageEditText = findViewById<EditText>(R.id.ageEditText)
        val genderEditText = findViewById<EditText>(R.id.genderEditText)
        val weightEditText = findViewById<EditText>(R.id.weightEditText)
        val heightEditText = findViewById<EditText>(R.id.heightEditText)
        val smokingCheckBox = findViewById<CheckBox>(R.id.smokingCheckBox)
        val alcoholCheckBox = findViewById<CheckBox>(R.id.alcoholCheckBox)
        val consentCheckBox = findViewById<CheckBox>(R.id.consentCheckBox)
        val privacyPolicyCheckBox = findViewById<CheckBox>(R.id.privacyPolicyCheckBox)
        val submitButton = findViewById<Button>(R.id.submitBtn)

        submitButton.setOnClickListener {
            val userDetail = Persona(
                name = nameEditText.text.toString(),
                age = ageEditText.text.toString().toInt(),
                gender = genderEditText.text.toString(),
                weight = weightEditText.text.toString().toDouble(),
                height = heightEditText.text.toString().toDouble(),
                smoking = smokingCheckBox.isChecked,
                alcoholConsumption = alcoholCheckBox.isChecked,
                consentDataProcessing = consentCheckBox.isChecked,
                privacyPolicyAgreement = privacyPolicyCheckBox.isChecked
            )
            userDetailsBox.put(userDetail)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
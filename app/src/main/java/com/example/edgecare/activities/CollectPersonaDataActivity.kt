package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.ObjectBox
import com.example.edgecare.R
import com.example.edgecare.models.Persona
import io.objectbox.Box
import io.objectbox.BoxStore

class CollectPersonaDataActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var ageEditText: EditText
    private lateinit var genderEditText: EditText
    private lateinit var weightEditText: EditText
    private lateinit var heightEditText: EditText
    private lateinit var smokingCheckBox: CheckBox
    private lateinit var alcoholCheckBox: CheckBox

    private lateinit var submitButton: Button
    private lateinit var editButton: Button

    private lateinit var boxStore: BoxStore
    private lateinit var userDetailsBox: Box<Persona>

    private var userDataExists: Boolean = false
    private var firstUserDetail: Persona? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_collect_persona_data)

        nameEditText = findViewById<EditText>(R.id.nameEditText)
        ageEditText = findViewById<EditText>(R.id.ageEditText)
        genderEditText = findViewById<EditText>(R.id.genderEditText)
        weightEditText = findViewById<EditText>(R.id.weightEditText)
        heightEditText = findViewById<EditText>(R.id.heightEditText)
        smokingCheckBox = findViewById<CheckBox>(R.id.smokingCheckBox)
        alcoholCheckBox = findViewById<CheckBox>(R.id.alcoholCheckBox)

        submitButton = findViewById<Button>(R.id.submitBtn)
        editButton = findViewById<Button>(R.id.editBtn)

        boxStore = ObjectBox.store
        userDetailsBox = boxStore.boxFor(Persona::class.java)

        userDataExists = loadDataAndLockFields(userDetailsBox)

        if (userDataExists) {
            editButton.visibility = View.VISIBLE // Show edit button if data exists
        } else {
            submitButton.visibility = View.VISIBLE // Show submit button if no data exists
        }

        editButton.setOnClickListener {
            enableEditing()
            submitButton.visibility = View.VISIBLE
            editButton.visibility = View.GONE
        }

        submitButton.setOnClickListener {
            saveUserDetails()
            submitButton.visibility = View.GONE
            editButton.visibility = View.VISIBLE
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadDataAndLockFields(userDetailsBox:Box<Persona>):Boolean {
        firstUserDetail = userDetailsBox.query().build().findFirst()
        firstUserDetail?.let {

            nameEditText.setText(it.name)
            ageEditText.setText(it.age.toString())
            genderEditText.setText(it.gender)
            weightEditText.setText(it.weight.toString())
            heightEditText.setText(it.height.toString())
            smokingCheckBox.isChecked = it.smoking
            alcoholCheckBox.isChecked = it.alcoholConsumption

            // Disabling fields to prevent editing
            nameEditText.isEnabled = false
            ageEditText.isEnabled = false
            genderEditText.isEnabled = false
            weightEditText.isEnabled = false
            heightEditText.isEnabled = false
            smokingCheckBox.isEnabled = false
            alcoholCheckBox.isEnabled = false

            return true
        }
        return false
    }

    private fun enableEditing(){

        nameEditText.isEnabled = true
        ageEditText.isEnabled = true
        genderEditText.isEnabled = true
        weightEditText.isEnabled = true
        heightEditText.isEnabled = true
        smokingCheckBox.isEnabled = true
        alcoholCheckBox.isEnabled = true
    }

    private fun saveUserDetails(){
        if(userDataExists) {
            firstUserDetail?.let {
                it.name = nameEditText.text.toString()
                it.age = ageEditText.text.toString().toInt()
                it.gender = genderEditText.text.toString()
                it.weight = weightEditText.text.toString().toDouble()
                it.height = heightEditText.text.toString().toDouble()
                it.smoking = smokingCheckBox.isChecked
                it.alcoholConsumption = alcoholCheckBox.isChecked
                userDetailsBox.put(it)
            }
        }
        else {
            val userDetail = Persona(
                name = nameEditText.text.toString(),
                age = ageEditText.text.toString().toInt(),
                gender = genderEditText.text.toString(),
                weight = weightEditText.text.toString().toDouble(),
                height = heightEditText.text.toString().toDouble(),
                smoking = smokingCheckBox.isChecked,
                alcoholConsumption = alcoholCheckBox.isChecked,
            )
            userDetailsBox.put(userDetail)
        }
    }

}
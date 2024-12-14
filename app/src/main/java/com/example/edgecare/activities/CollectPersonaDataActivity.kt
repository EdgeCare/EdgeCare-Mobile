package com.example.edgecare.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.edgecare.ObjectBox
import com.example.edgecare.databinding.ActivityCollectPersonaDataBinding
import com.example.edgecare.models.Gender
import com.example.edgecare.models.Persona
import io.objectbox.Box
import io.objectbox.BoxStore

class CollectPersonaDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCollectPersonaDataBinding
    private lateinit var boxStore: BoxStore
    private lateinit var userDetailsBox: Box<Persona>

    private var userDataExists: Boolean = false
    private var firstUserDetail: Persona? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize View Binding
        binding = ActivityCollectPersonaDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        boxStore = ObjectBox.store
        userDetailsBox = boxStore.boxFor(Persona::class.java)

        userDataExists = loadDataAndLockFields()

        if (userDataExists) {
            binding.editBtn.visibility = View.VISIBLE // Show edit button if data exists
        } else {
            binding.submitBtn.visibility = View.VISIBLE // Show submit button if no data exists
        }

        binding.editBtn.setOnClickListener {
            enableEditing()
            binding.submitBtn.visibility = View.VISIBLE
            binding.editBtn.visibility = View.GONE
        }

        binding.submitBtn.setOnClickListener {
            saveUserDetails()
            binding.submitBtn.visibility = View.GONE
            binding.editBtn.visibility = View.VISIBLE
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadDataAndLockFields(): Boolean {
        firstUserDetail = userDetailsBox.query().build().findFirst()
        firstUserDetail?.let {
            with(binding) {
                nameEditText.setText(it.name)
                ageEditText.setText(it.age.toString())
                if(it.gender == Gender.MALE.displayName)
                    radioButtonMale.isChecked = true
                else if(it.gender == Gender.FEMALE.displayName)
                    radioButtonFemale.isChecked = true
                weightEditText.setText(it.weight.toString())
                heightEditText.setText(it.height.toString())
                smokingCheckBox.isChecked = it.smoking
                alcoholCheckBox.isChecked = it.alcoholConsumption

                // Disabling fields to prevent editing
                nameEditText.isEnabled = false
                ageEditText.isEnabled = false
                radioButtonMale.isEnabled = false
                radioButtonFemale.isEnabled = false
                weightEditText.isEnabled = false
                heightEditText.isEnabled = false
                smokingCheckBox.isEnabled = false
                alcoholCheckBox.isEnabled = false
            }
            return true
        }
        return false
    }

    private fun enableEditing() {
        with(binding) {
            nameEditText.isEnabled = true
            ageEditText.isEnabled = true
            radioButtonMale.isEnabled = true
            radioButtonFemale.isEnabled = true
            weightEditText.isEnabled = true
            heightEditText.isEnabled = true
            smokingCheckBox.isEnabled = true
            alcoholCheckBox.isEnabled = true
        }
    }

    private fun saveUserDetails() {
        if (userDataExists) {
            firstUserDetail?.let {
                with(binding) {
                    it.name = nameEditText.text.toString()
                    it.age = ageEditText.text.toString().toInt()
                    if(radioButtonMale.isChecked)
                        it.gender = Gender.MALE.displayName
                    else if (radioButtonFemale.isChecked)
                        it.gender = Gender.FEMALE.displayName
                    it.weight = weightEditText.text.toString().toDouble()
                    it.height = heightEditText.text.toString().toDouble()
                    it.smoking = smokingCheckBox.isChecked
                    it.alcoholConsumption = alcoholCheckBox.isChecked
                }
                userDetailsBox.put(it)
            }
        } else {
            val gender:String?
            if(binding.radioButtonMale.isChecked)
                gender = Gender.MALE.displayName
            else if (binding.radioButtonFemale.isChecked)
                gender = Gender.FEMALE.displayName
            else
                gender = null
            val userDetail = Persona(
                name = binding.nameEditText.text.toString(),
                age = binding.ageEditText.text.toString().toInt(),
                gender = gender,
                weight = binding.weightEditText.text.toString().toDouble(),
                height = binding.heightEditText.text.toString().toDouble(),
                smoking = binding.smokingCheckBox.isChecked,
                alcoholConsumption = binding.alcoholCheckBox.isChecked
            )
            userDetailsBox.put(userDetail)
        }
    }
}
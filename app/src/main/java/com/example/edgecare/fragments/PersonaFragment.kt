package com.example.edgecare.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.edgecare.ObjectBox
import com.example.edgecare.activities.MainActivity
import com.example.edgecare.databinding.FragmentPersonaBinding
import com.example.edgecare.models.Gender
import com.example.edgecare.models.Persona
import io.objectbox.Box
import io.objectbox.BoxStore

class PersonaFragment : Fragment() {

    private var _binding: FragmentPersonaBinding? = null
    private val binding get() = _binding!!

    private lateinit var boxStore: BoxStore
    private lateinit var userDetailsBox: Box<Persona>

    private var userDataExists: Boolean = false
    private var firstUserDetail: Persona? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize View Binding
        _binding = FragmentPersonaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
            if (validatePersonaData()) {
                saveUserDetails()
                binding.submitBtn.visibility = View.GONE
                binding.editBtn.visibility = View.VISIBLE
                // Assuming navigation to MainActivity
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun loadDataAndLockFields(): Boolean {
        firstUserDetail = userDetailsBox.query().build().findFirst()
        firstUserDetail?.let {
            with(binding) {
                nameEditText.setText(it.name)
                ageEditText.setText(it.age.toString())
                if (Gender.fromDisplayName(it.gender) == Gender.MALE)
                    radioButtonMale.isChecked = true
                else if (Gender.fromDisplayName(it.gender) == Gender.FEMALE)
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

    private fun validatePersonaData(): Boolean {
        with(binding) {
            if (nameEditText.text.toString().isBlank()) {
                nameEditText.error = "Name cannot be empty"
                return false
            }

            val age = ageEditText.text.toString().toIntOrNull()
            if (age == null || age <= 0 || age > 120) {
                ageEditText.error = "Please enter a valid age (1-120)"
                return false
            }

            if (!radioButtonMale.isChecked && !radioButtonFemale.isChecked) {
                Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
                return false
            }

            val weight = weightEditText.text.toString().toDoubleOrNull()
            if (weight == null || weight <= 0 || weight > 500) {
                weightEditText.error = "Please enter a valid weight (1-500) kg"
                return false
            }

            val height = heightEditText.text.toString().toDoubleOrNull()
            if (height == null || height <= 0 || height > 300) {
                heightEditText.error = "Please enter a valid height (1-300) cm"
                return false
            }
        }
        return true
    }

    private fun saveUserDetails() {
        if (userDataExists) {
            firstUserDetail?.let {
                with(binding) {
                    it.name = nameEditText.text.toString()
                    it.age = ageEditText.text.toString().toInt()
                    it.gender = if (binding.radioButtonMale.isChecked) Gender.MALE.displayName else Gender.FEMALE.displayName
                    it.weight = weightEditText.text.toString().toDouble()
                    it.height = heightEditText.text.toString().toDouble()
                    it.smoking = smokingCheckBox.isChecked
                    it.alcoholConsumption = alcoholCheckBox.isChecked
                }
                userDetailsBox.put(it)
            }
        } else {
            val userDetail = Persona(
                name = binding.nameEditText.text.toString(),
                age = binding.ageEditText.text.toString().toInt(),
                gender = if (binding.radioButtonMale.isChecked) Gender.MALE.displayName else Gender.FEMALE.displayName,
                weight = binding.weightEditText.text.toString().toDouble(),
                height = binding.heightEditText.text.toString().toDouble(),
                smoking = binding.smokingCheckBox.isChecked,
                alcoholConsumption = binding.alcoholCheckBox.isChecked
            )
            userDetailsBox.put(userDetail)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
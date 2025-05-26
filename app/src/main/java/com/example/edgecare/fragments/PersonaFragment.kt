package com.example.edgecare.fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.edgecare.ObjectBox
import com.example.edgecare.activities.MainActivity
import com.example.edgecare.api.sendUserPersona
import com.example.edgecare.databinding.FragmentPersonaBinding
import com.example.edgecare.models.Gender
import com.example.edgecare.models.Persona
import com.example.edgecare.utils.AnonymizationUtils.anonymizeAge
import com.example.edgecare.utils.AnonymizationUtils.calculateAge
import io.objectbox.Box
import io.objectbox.BoxStore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PersonaFragment : Fragment() {

    private var _binding: FragmentPersonaBinding? = null
    private val binding get() = _binding!!

    private lateinit var boxStore: BoxStore
    private lateinit var userDetailsBox: Box<Persona>

    private var userDataExists: Boolean = false
    private var firstUserDetail: Persona? = null

    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize View Binding
        _binding = FragmentPersonaBinding.inflate(inflater, container, false)

        // DatePickerDialog OnClickListener
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            calendar.set(year, month, day)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            binding.birthdayEditText.setText(dateFormat.format(calendar.time))
        }
        binding.birthdayEditText.setOnClickListener {
            DatePickerDialog(
                requireContext(), dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

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
                birthdayEditText.setText(it.birthday.toString())
                if (it.gender?.let { it1 -> Gender.fromDisplayName(it1) } == Gender.MALE)
                    radioButtonMale.isChecked = true
                else if (it.gender?.let { it1 -> Gender.fromDisplayName(it1) } == Gender.FEMALE)
                    radioButtonFemale.isChecked = true
                if(it.weight != null){weightEditText.setText(it.weight.toString())}
                if(it.height != null)heightEditText.setText(it.height.toString())
                if(it.sleepHours != null)sleepHoursEditText.setText(it.sleepHours.toString())
                allergiesEditText.setText(it.allergies)
                smokingCheckBox.isChecked = it.smoking == true
                alcoholCheckBox.isChecked = it.alcoholConsumption == true
                diabetesCheckBox.isChecked = it.diabetes == true
                highBloodPressureCheckBox.isChecked = it.highBloodPressure == true

                // Disabling fields to prevent editing
                nameEditText.isEnabled = false
                birthdayEditText.isEnabled = false
                radioButtonMale.isEnabled = false
                radioButtonFemale.isEnabled = false
                weightEditText.isEnabled = false
                heightEditText.isEnabled = false
                sleepHoursEditText.isEnabled = false
                allergiesEditText.isEnabled = false
                smokingCheckBox.isEnabled = false
                alcoholCheckBox.isEnabled = false
                diabetesCheckBox.isEnabled= false
                highBloodPressureCheckBox.isEnabled= false
            }
            return true
        }
        return false
    }

    private fun enableEditing() {
        with(binding) {
            nameEditText.isEnabled = true
            birthdayEditText.isEnabled = true
            radioButtonMale.isEnabled = true
            radioButtonFemale.isEnabled = true
            weightEditText.isEnabled = true
            heightEditText.isEnabled = true
            sleepHoursEditText.isEnabled = true
            allergiesEditText.isEnabled = true
            smokingCheckBox.isEnabled = true
            alcoholCheckBox.isEnabled = true
            diabetesCheckBox.isEnabled= true
            highBloodPressureCheckBox.isEnabled= true
        }
    }

    private fun validatePersonaData(): Boolean {
        with(binding) {
            if (nameEditText.text.toString().isBlank()) {
                nameEditText.error = "Name cannot be empty"
                return false
            }

//            val age = ageEditText.text.toString().toIntOrNull()
//            if (age == null || age <= 0 || age > 120) {
//                ageEditText.error = "Please enter a valid age (1-120)"
//                return false
//            }

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

            val sleepHours = sleepHoursEditText.text.toString().toDoubleOrNull()
            if (sleepHours == null || sleepHours <= 0 || sleepHours > 24) {
                sleepHoursEditText.error = "Please enter a valid sleeping hour count"
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
                    it.birthday = birthdayEditText.text.toString()
                    it.gender = if (binding.radioButtonMale.isChecked) Gender.MALE.displayName else Gender.FEMALE.displayName
                    it.weight = weightEditText.text.toString().toDouble()
                    it.height = heightEditText.text.toString().toDouble()
                    it.sleepHours = sleepHoursEditText.text.toString().toDouble()
                    it.allergies = allergiesEditText.text.toString()
                    it.smoking = smokingCheckBox.isChecked
                    it.alcoholConsumption = alcoholCheckBox.isChecked
                    it.diabetes = diabetesCheckBox.isChecked
                    it.highBloodPressure = highBloodPressureCheckBox.isChecked
                }
                userDetailsBox.put(it)
                sendUserPersona(personaToString(it), requireContext()) { response ->
                    if (response == null) {
                        println("error while saving user details")
                    }
                }
            }
        } else {
            val userDetail = Persona(
                name = binding.nameEditText.text.toString(),
                birthday = binding.birthdayEditText.text.toString(),
                gender = if (binding.radioButtonMale.isChecked) Gender.MALE.displayName else Gender.FEMALE.displayName,
                weight = binding.weightEditText.text.toString().toDouble(),
                height = binding.heightEditText.text.toString().toDouble(),
                sleepHours = binding.sleepHoursEditText.text.toString().toDouble(),
                allergies = binding.allergiesEditText.text.toString(),
                smoking = binding.smokingCheckBox.isChecked,
                alcoholConsumption = binding.alcoholCheckBox.isChecked,
                diabetes = binding.diabetesCheckBox.isChecked,
                highBloodPressure = binding.highBloodPressureCheckBox.isChecked
            )
            userDetailsBox.put(userDetail)
            sendUserPersona(personaToString(userDetail),requireContext()) { response ->
                if (response == null) {
                    println("error while saving user details")
                }
            }
        }
    }

    private fun personaToString(persona: Persona): String {
        val age = persona.birthday?.let { calculateAge(it) }
        val ageString = age?.let { anonymizeAge(it) }
        return """ Age range: ${ageString ?: "N/A"} , Gender: ${persona.gender ?: "N/A"} , Weight: ${persona.weight ?: "N/A"} kg , Height: ${persona.height ?: "N/A"} cm , Sleeping hours: ${persona.sleepHours?: "N/A"}, Allergies: ${persona.allergies ?: "None"} , Diabetes: ${if (persona.diabetes == true) "Yes" else "No"} , High Blood Pressure: ${if (persona.highBloodPressure == true) "Yes" else "No"} , Smoking: ${if (persona.smoking == true) "Yes" else "No"} , Alcohol Consumption: ${if (persona.alcoholConsumption == true) "Yes" else "No"} ,Sleep Hours: ${persona.sleepHours ?: "N/A"} hours """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

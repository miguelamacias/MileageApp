package com.macisdev.mileageapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.macisdev.mileageapp.databinding.FragmentAddMileageBinding
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle
import java.util.*

class AddMileageFragment : Fragment() {

	private lateinit var gui: FragmentAddMileageBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentAddMileageBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.vehicleSpinner.adapter = ArrayAdapter(
			view.context, android.R.layout.simple_spinner_dropdown_item, MileageRepository.getVehicles()
		)

		gui.kilometresEditText.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(sequence: Editable?) {
				if(gui.litresEditText.text.isNotEmpty()) calculateMileage()
			}

			override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {}
		})

		gui.litresEditText.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(sequence: Editable?) {
				if(gui.kilometresEditText.text.isNotEmpty()) calculateMileage()
			}

			override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {}
		})

		gui.saveMileageButton.setOnClickListener {
			val mileageData = gui.mileageResultEditText.text.toString().toDoubleOrNull()

			if (mileageData != null){
				val vehicle = gui.vehicleSpinner.selectedItem as Vehicle
				val date = Date()
				val kilometres = gui.kilometresEditText.text.toString().toDouble()
				val litres = gui.litresEditText.text.toString().toDouble()


				MileageRepository.storeMileage(Mileage(vehicle, date, mileageData, kilometres, litres))
			}
		}
	}

	private fun calculateMileage() {
		try {
			val kilometres = gui.kilometresEditText.text.toString().toDouble()
			val litres = gui.litresEditText.text.toString().toDouble()
			val mileage = 100 * litres / kilometres

			if (mileage > 0) {
				gui.mileageResultEditText.setText(String.format(Locale.getDefault(),"%.2f", mileage))
				gui.saveMileageButton.isEnabled = true
			} else {
				gui.mileageResultEditText.setText(R.string.wrong_value)
				gui.saveMileageButton.isEnabled = false
			}

		} catch (e: NumberFormatException) {
			gui.mileageResultEditText.setText(R.string.wrong_value)
			gui.saveMileageButton.isEnabled = false
		}
	}

	companion object {
		fun newInstance() = AddMileageFragment()
	}
}
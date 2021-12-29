package com.macisdev.mileageapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.macisdev.mileageapp.databinding.FragmentAddMileageBinding
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle
import java.text.DecimalFormat
import java.text.ParseException
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
			val decimalFormatter = DecimalFormat.getInstance(Locale.getDefault())

			val mileageData = decimalFormatter.parse(gui.mileageResultEditText.text.toString())?.toDouble() ?: -1.0

			if (mileageData > 0) {
				val vehicle = gui.vehicleSpinner.selectedItem as Vehicle
				val date = Date()
				val kilometres = decimalFormatter.parse(gui.kilometresEditText.text.toString())?.toDouble() ?: -1.0
				val litres = decimalFormatter.parse(gui.litresEditText.text.toString())?.toDouble() ?: -1.0

				MileageRepository.storeMileage(Mileage(vehicle, date, mileageData, kilometres, litres))

				Snackbar.make(gui.root, R.string.mileage_added, Snackbar.LENGTH_LONG).show()

				val directions = AddMileageFragmentDirections.actionAddMileageFragmentPop()
				findNavController().navigate(directions)
			}
		}

		gui.dateEditText.apply {
			setText(DateFormat.format("dd/MM/yyyy", Date())) //TODO: User should be able to change the date
			setOnClickListener {
				Toast.makeText(context, R.string.not_available, Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun calculateMileage() {
		try {
			val decimalFormatter = DecimalFormat.getInstance(Locale.getDefault())

			val kilometres = decimalFormatter.parse(gui.kilometresEditText.text.toString())?.toDouble() ?: 0.0
			val litres = decimalFormatter.parse(gui.litresEditText.text.toString())?.toDouble() ?: 0.0
			val mileage = 100 * litres / kilometres

			if (mileage > 0) {
				gui.mileageResultEditText.setText(String.format(Locale.getDefault(),"%.2f", mileage))
				gui.saveMileageButton.isEnabled = true
			} else {
				gui.mileageResultEditText.setText(R.string.wrong_value)
				gui.saveMileageButton.isEnabled = false
			}

		} catch (e: ParseException) {
			gui.mileageResultEditText.setText(R.string.wrong_value)
			gui.saveMileageButton.isEnabled = false
		}
	}
}
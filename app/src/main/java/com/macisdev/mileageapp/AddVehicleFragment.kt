package com.macisdev.mileageapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.databinding.FragmentAddVehicleBinding
import com.macisdev.mileageapp.model.Vehicle

class AddVehicleFragment : DialogFragment() {

	private lateinit var gui: FragmentAddVehicleBinding
	private val mileageRepository = MileageRepository.get()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentAddVehicleBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.addVehicleButton.setOnClickListener {
			addVehicle()
		}
	}

	private fun addVehicle() {
		var emptyField = false

		val plateNumber = gui.plateEditText.text.toString()
		if (plateNumber.isBlank()) {
			gui.plateEditText.error = getString(R.string.error_empty_field)
			emptyField = true
		}

		val maker = gui.makerEditText.text.toString()
		if (maker.isBlank()) {
			gui.makerEditText.error = getString(R.string.error_empty_field)
			emptyField = true
		}

		val model = gui.modelEditText.text.toString()
		if (model.isBlank()) {
			gui.modelEditText.error = getString(R.string.error_empty_field)
			emptyField = true
		}

		if (!emptyField) {
			val vehicle = Vehicle(plateNumber, maker, model)

			mileageRepository.addVehicle(vehicle)

			parentFragment?.view?.let {
					parentView -> Snackbar.make(parentView, R.string.vehicle_added, Snackbar.LENGTH_LONG).show()
			}

			findNavController().navigateUp()
		}
	}
}
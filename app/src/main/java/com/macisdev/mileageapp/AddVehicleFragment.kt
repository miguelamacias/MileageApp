package com.macisdev.mileageapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.macisdev.mileageapp.databinding.FragmentAddVehicleBinding
import com.macisdev.mileageapp.model.Vehicle

class AddVehicleFragment : DialogFragment() {

	interface Callbacks {
		fun onVehicleAdded(vehicle: Vehicle)
	}

	private lateinit var gui: FragmentAddVehicleBinding
	private var parentActivityCallbacks: Callbacks? = null
	private val mileageRepository = MileageRepository.get()

	override fun onAttach(context: Context) {
		super.onAttach(context)
		parentActivityCallbacks = context as Callbacks
	}

	override fun onDetach() {
		super.onDetach()
		parentActivityCallbacks = null
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentAddVehicleBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.addVehicleButton.setOnClickListener {
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
				parentActivityCallbacks?.onVehicleAdded(vehicle)

				parentFragment?.view?.let {
						parentView -> Snackbar.make(parentView, R.string.vehicle_added, Snackbar.LENGTH_LONG).show()
				}

				findNavController().navigateUp()
			}
		}
	}
}
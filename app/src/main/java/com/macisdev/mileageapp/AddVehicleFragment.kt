package com.macisdev.mileageapp

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.macisdev.mileageapp.databinding.FragmentAddVehicleBinding
import com.macisdev.mileageapp.model.Vehicle
import com.macisdev.mileageapp.utils.Adapters
import com.macisdev.mileageapp.viewModels.AddVehicleViewModel

class AddVehicleFragment : DialogFragment() {

	private lateinit var gui: FragmentAddVehicleBinding
	private val addVehicleVM: AddVehicleViewModel by viewModels()
	private val fragmentArgs: AddVehicleFragmentArgs by navArgs()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentAddVehicleBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.plateEditText.filters += InputFilter.AllCaps()

		val iconAdapter = Adapters.IconAdapter(view.context, 0, addVehicleVM.icons)
		gui.iconSpinner.adapter = iconAdapter

		val colorAdapter = Adapters.ColorAdapter(view.context, 0, addVehicleVM.colors)
		gui.colorSpinner.adapter = colorAdapter

		if (fragmentArgs.editMode) {
			addVehicleVM.getVehicle(fragmentArgs.editVehiclePlate)
				.observe(viewLifecycleOwner, {editVehicle(it, iconAdapter, colorAdapter)})

		} else {
			gui.addVehicleButton.setOnClickListener {
				addVehicle()
			}
		}
	}

	private fun editVehicle(vehicle: Vehicle, iconAdapter: Adapters.IconAdapter, colorAdapter: Adapters.ColorAdapter) {
		gui.plateEditText.apply {
			setText(vehicle.plateNumber)
			isEnabled = false
		}

		gui.makerEditText.setText(vehicle.maker)
		gui.modelEditText.setText(vehicle.model)

		gui.iconSpinner.setSelection(iconAdapter.getPosition(vehicle.icon))
		gui.colorSpinner.setSelection(colorAdapter.getPosition(vehicle.iconColor))

		gui.addVehicleButton.apply {
			setText(R.string.save_changes)
			setOnClickListener {
				var emptyField = false
				val maker = gui.makerEditText.text.toString().trim()
				if (maker.isBlank()) {
					gui.makerEditText.error = getString(R.string.error_empty_field)
					emptyField = true
				}

				val model = gui.modelEditText.text.toString().trim()
				if (model.isBlank()) {
					gui.modelEditText.error = getString(R.string.error_empty_field)
					emptyField = true
				}

				if (!emptyField) {
					vehicle.maker = maker
					vehicle.model = model
					vehicle.icon = gui.iconSpinner.selectedItem as Int
					vehicle.iconColor = gui.colorSpinner.selectedItem as Int

					addVehicleVM.updateVehicle(vehicle)

					parentFragment?.view?.let {
							parentView ->
						Snackbar.make(parentView, R.string.vehicle_edited, Snackbar.LENGTH_LONG).show()
					}

					findNavController().navigateUp()
				}
			}
		}
	}

	private fun addVehicle() {
		var emptyField = false

		val plateNumber = gui.plateEditText.text.toString().trim()
		if (plateNumber.isBlank()) {
			gui.plateEditText.error = getString(R.string.error_empty_field)
			emptyField = true
		}

		val maker = gui.makerEditText.text.toString().trim()
		if (maker.isBlank()) {
			gui.makerEditText.error = getString(R.string.error_empty_field)
			emptyField = true
		}

		val model = gui.modelEditText.text.toString().trim()
		if (model.isBlank()) {
			gui.modelEditText.error = getString(R.string.error_empty_field)
			emptyField = true
		}

		val icon = gui.iconSpinner.selectedItem as Int
		val color = gui.colorSpinner.selectedItem as Int

		if (!emptyField) {
			val vehicle = Vehicle(plateNumber, maker, model, icon, color)

			addVehicleVM.storeVehicle(vehicle)

			parentFragment?.view?.let {
					parentView -> Snackbar.make(parentView, R.string.vehicle_added, Snackbar.LENGTH_LONG).show()
			}

			findNavController().navigateUp()
		}
	}


}
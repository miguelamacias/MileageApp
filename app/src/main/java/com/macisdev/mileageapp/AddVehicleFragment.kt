package com.macisdev.mileageapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.macisdev.mileageapp.databinding.FragmentAddVehicleBinding
import com.macisdev.mileageapp.model.Vehicle

class AddVehicleFragment : DialogFragment() {
	private lateinit var gui: FragmentAddVehicleBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentAddVehicleBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		gui.addVehicleButton.setOnClickListener {
			val plateNumber = gui.plateEditText.text.toString()
			val maker = gui.makerEditText.text.toString()
			val model = gui.modelEditText.text.toString()
			MileageRepository.addVehicle(Vehicle(plateNumber, maker, model))
			dismiss()
		}
	}
}
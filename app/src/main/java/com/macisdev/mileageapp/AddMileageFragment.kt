package com.macisdev.mileageapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.macisdev.mileageapp.databinding.FragmentAddMileageBinding

class AddMileageFragment : Fragment() {

	private lateinit var gui: FragmentAddMileageBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		gui = FragmentAddMileageBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.vehicleSpinner.adapter = ArrayAdapter(
			view.context, android.R.layout.simple_spinner_dropdown_item, MileageRepository.vehicles
		)
	}

	companion object {
		fun newInstance() = AddMileageFragment()
	}
}
package com.macisdev.mileageapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

private const val ARG_VEHICLE = "vehicle_arg"

class MileageListFragment : Fragment() {
	private var param1: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			param1 = it.getString(ARG_VEHICLE)
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_mileage_list, container, false)
	}

	companion object {
		fun newInstance(vehiclePlate: String) =
			MileageListFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_VEHICLE, vehiclePlate)
				}
			}
	}
}
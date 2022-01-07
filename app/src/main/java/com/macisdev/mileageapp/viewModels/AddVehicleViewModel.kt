package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.R
import com.macisdev.mileageapp.database.MileageRepository

class AddVehicleViewModel : ViewModel() {
	val mileageRepository = MileageRepository.get()

	val icons = listOf(
		R.drawable.ic_vehicle_car_hatchback,
		R.drawable.ic_vehicle_car_sedan,
		R.drawable.ic_vehicle_car_wagon,
		R.drawable.ic_vehicle_car_sport,
		R.drawable.ic_vehicle_car_pickup,
		R.drawable.ic_vehicle_car_jeep,
		R.drawable.ic_vehicle_car_convertible,
		R.drawable.ic_vehicle_motorcycle_scooter,
		R.drawable.ic_vehicle_motorcycle_sport,
		R.drawable.ic_vehicle_motorcycle_offroad,
		R.drawable.ic_vehicle_van_small,
		R.drawable.ic_vehicle_van_big,
		R.drawable.ic_vehicle_van_vintage,
		R.drawable.ic_vehicle_lorry_small,
		R.drawable.ic_vehicle_lorry_trailer,
		R.drawable.ic_vehicle_bus_simple,
		R.drawable.ic_vehicle_bus_double_decker,
		R.drawable.ic_vehicle_taxi
	)
	val colors = listOf(
		R.color.black,
		R.color.grey,
		R.color.silver,
		R.color.white,
		R.color.champagne,
		R.color.brown,
		R.color.red,
		R.color.green,
		R.color.dark_blue,
		R.color.blue,
		R.color.orange,
		R.color.yellow,
		R.color.purple,
	)
}
package com.macisdev.mileageapp

import android.content.Context
import android.util.Log
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle

object MileageRepository {

	private val vehicles = mutableListOf<Vehicle>(
		Vehicle("8054FDG", "Ford", "Mondeo"),
		Vehicle("5687JRK", "Honda", "PCX"),
		Vehicle("SO0728G", "Rover", "220")
	)

	private val mileages = mutableListOf<Mileage>()

	fun getVehicles() = vehicles

	fun getVehicle(plateNumber: String): Vehicle? {
		return vehicles.find { it.plateNumber == plateNumber }
	}

	fun storeMileage(mileage: Mileage) {
		mileages.add(mileage)
		Log.d(MainActivity.TAG, mileages.toString())
	}

	fun getMileages(plateNumber: String): List<Mileage> {
		return mileages.filter { it.vehicle.plateNumber == plateNumber}
	}


	operator fun invoke(context: Context) : MileageRepository {
		return this
	}
}
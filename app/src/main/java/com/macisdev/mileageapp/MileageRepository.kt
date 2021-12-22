package com.macisdev.mileageapp

import android.content.Context
import android.util.Log
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle
import java.util.*

object MileageRepository {

	private val vehicles = mutableListOf(
		Vehicle("8054FDG", "Ford", "Mondeo"),
		Vehicle("5687JRK", "Honda", "PCX"),
		Vehicle("SO0728G", "Rover", "220")
	)

	private val mileages = mutableListOf(
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.54,728.4, 58.11),
		Mileage(Vehicle("5687JRK", "Honda", "PCX"), Date(), 2.47,328.12, 7.95),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 6.98,630.5, 51.78),
		Mileage(Vehicle("SO0728G", "Rover", "220"), Date(), 5.80,650.9, 39.25),
	)

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
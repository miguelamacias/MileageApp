package com.macisdev.mileageapp

import android.content.Context
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
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 6.54,728.4, 58.11, "Travel to Madrid"),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.55,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 6.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 6.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 5.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 6.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.56,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 8.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 5.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 8.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 6.89,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.54,728.4, 58.11, "Broken turbo hose"),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 6.14,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 4.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.75,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 8.76,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.42,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.35,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.75,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 8.87,728.4, 58.11, "ITV"),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.16,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.54,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.88,728.4, 58.11),
		Mileage(Vehicle("8054FDG", "Ford", "Mondeo"), Date(), 7.11,728.4, 58.11),
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
	}

	fun getMileages(plateNumber: String): List<Mileage> {
		return mileages.filter { it.vehicle.plateNumber == plateNumber}
	}

	fun addVehicle(vehicle: Vehicle) {
		vehicles.add(vehicle)
	}

	operator fun invoke(context: Context) : MileageRepository {
		return this
	}
}
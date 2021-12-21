package com.macisdev.mileageapp

import android.content.Context
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle

object MileageRepository {

	val vehicles = mutableListOf<Vehicle>(
		Vehicle("8054FDG", "Ford", "Mondeo"),
		Vehicle("5687JRK", "Honda", "PCX"),
		Vehicle("SO0728G", "Rover", "220")
	)

	val mileages = mutableListOf<Mileage>()

	operator fun invoke(context: Context) : MileageRepository {
		return this
	}
}
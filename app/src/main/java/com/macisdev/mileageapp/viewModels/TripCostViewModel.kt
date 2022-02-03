package com.macisdev.mileageapp.viewModels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.database.MileageRepository

class TripCostViewModel : ViewModel() {
	var origin = ""
	var destination = ""
	var originAddress = ""
	var destinationAddress = ""

	var resultsCardViewVisibility = View.GONE
	var currentTripDistance = ""
	var currentTripFuel = ""
	var currentTripDuration = ""
	var currentTripCost = ""

	private val mileageRepository = MileageRepository.get()

	fun getVehicles() = mileageRepository.getVehicles()

	fun getVehicleAverageMileage(plateNumber: String) = mileageRepository.getVehicleAverageMileage(plateNumber)

	fun getTripDistance(avoidTolls: Boolean) = mileageRepository.getTripDistance(origin, destination, avoidTolls)

	fun getTripDuration(): LiveData<Int> = mileageRepository.tripDuration
}
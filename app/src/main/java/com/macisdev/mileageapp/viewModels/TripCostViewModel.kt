package com.macisdev.mileageapp.viewModels

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.api.fuel.FuelStation
import com.macisdev.mileageapp.database.AppDataRepository

class TripCostViewModel : ViewModel() {
	var originCode = ""
	var destinationCode = ""
	var originFullAddress = ""
	var destinationFullAddress = ""
	var originName = ""
	var destinationName = ""

	var resultsCardViewVisibility = View.GONE
	var sharedTripCostVisibility = View.GONE
	var currentTripDistance = ""
	var currentTripFuel = ""
	var currentTripDuration = ""
	var currentTripCost = ""
	var sharedTripCost = ""

	private val appDataRepository = AppDataRepository.get()

	fun getVehicles() = appDataRepository.getVehicles()

	fun getVehicleAverageMileage(plateNumber: String) = appDataRepository.getVehicleAverageMileage(plateNumber)

	fun getTripDistance(avoidTolls: Boolean) = appDataRepository.getTripDistance(originCode, destinationCode, avoidTolls)

	fun getTripDuration(): LiveData<Int> = appDataRepository.tripDuration

	fun getFuelStationById(cityCode: Int, stationId: Int): LiveData<FuelStation> =
		appDataRepository.getFuelStationById(cityCode, stationId)
}
package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.database.MileageRepository

class TripCostViewModel : ViewModel() {
	private val mileageRepository = MileageRepository.get()

	fun getVehicles() = mileageRepository.getVehicles()

	fun getVehicleAverageMileage(plateNumber: String) = mileageRepository.getVehicleAverageMileage(plateNumber)
}
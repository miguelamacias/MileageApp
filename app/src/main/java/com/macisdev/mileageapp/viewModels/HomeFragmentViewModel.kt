package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.macisdev.mileageapp.api.fuel.FuelStation
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle
import kotlinx.coroutines.launch

class HomeFragmentViewModel : ViewModel() {
	private val mileageRepository = MileageRepository.get()

	private var deletedVehicle: Vehicle? = null
	private var clearedMileages: List<Mileage>? = null

	val vehiclesList: LiveData<List<Vehicle>> = mileageRepository.getVehicles()

	fun getLastMileage() = mileageRepository.getLastMileage()

	fun getMileages(plateNumber: String) = mileageRepository.getMileages(plateNumber)

	fun getStatistics() = mileageRepository.getStatistics()

	fun getVehicle(plateNumber: String) = mileageRepository.getVehicle(plateNumber)

	fun deleteVehicle(vehicle: Vehicle?, mileages: List<Mileage>) {
		clearedMileages = mileages
		deletedVehicle = vehicle
		mileageRepository.deleteVehicle(vehicle?.plateNumber ?: "")
	}

	private fun restoreClearedMileages() {
		clearedMileages?.forEach { mileageRepository.storeMileage(it) }
	}

	fun restoreDeletedVehicle() {
		deletedVehicle?.let { viewModelScope.launch { mileageRepository.addVehicle(it) } }
		restoreClearedMileages()
	}

	fun getFuelStationById(cityCode: Int, stationId: Int): LiveData<FuelStation> =
		mileageRepository.getFuelStationById(cityCode, stationId)
}
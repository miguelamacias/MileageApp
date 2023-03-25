package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.api.fuel.FuelStation
import com.macisdev.mileageapp.database.AppDataRepository
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle
import java.util.*

class HomeFragmentViewModel : ViewModel() {
	private val appDataRepository = AppDataRepository.get()

	private var deletedVehicle: Vehicle? = null
	private var deletedMileages: List<Mileage> = emptyList()

	val vehiclesList: LiveData<List<Vehicle>> = appDataRepository.getVehicles()

	fun getLastMileage() = appDataRepository.getLastMileage()

	fun getMileages(plateNumber: String) = appDataRepository.getMileages(plateNumber)

	fun getStatistics() = appDataRepository.getStatistics()

	fun getVehicle(plateNumber: String) = appDataRepository.getVehicle(plateNumber)

	fun deleteVehicle(vehicle: Vehicle?, mileages: List<Mileage>) {
		deletedMileages = mileages
		deletedVehicle = vehicle
		appDataRepository.deleteVehicle(vehicle?.plateNumber ?: "")
	}

	fun restoreDeletedVehicle() {
		deletedVehicle?.let { vehicle ->
			appDataRepository.restoreVehicle(vehicle, deletedMileages)
		}
	}

	fun getFuelStationById(cityCode: Int, stationId: Int): LiveData<FuelStation> =
		appDataRepository.getFuelStationById(cityCode, stationId)

	fun getHistoricalData(date: Date, cityCode: Int, stationId: Int) =
		appDataRepository.getHistoricalData(date, cityCode, stationId)

}
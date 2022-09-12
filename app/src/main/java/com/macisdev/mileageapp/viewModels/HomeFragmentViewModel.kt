package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.api.fuel.FuelStation
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle
import java.util.*

class HomeFragmentViewModel : ViewModel() {
	private val mileageRepository = MileageRepository.get()

	private var deletedVehicle: Vehicle? = null
	private var deletedMileages: List<Mileage> = emptyList()

	val vehiclesList: LiveData<List<Vehicle>> = mileageRepository.getVehicles()

	fun getLastMileage() = mileageRepository.getLastMileage()

	fun getMileages(plateNumber: String) = mileageRepository.getMileages(plateNumber)

	fun getStatistics() = mileageRepository.getStatistics()

	fun getVehicle(plateNumber: String) = mileageRepository.getVehicle(plateNumber)

	fun deleteVehicle(vehicle: Vehicle?, mileages: List<Mileage>) {
		deletedMileages = mileages
		deletedVehicle = vehicle
		mileageRepository.deleteVehicle(vehicle?.plateNumber ?: "")
	}

	fun restoreDeletedVehicle() {
		deletedVehicle?.let { vehicle ->
			mileageRepository.restoreVehicle(vehicle, deletedMileages)
		}
	}

	fun getFuelStationById(cityCode: Int, stationId: Int): LiveData<FuelStation> =
		mileageRepository.getFuelStationById(cityCode, stationId)

	fun getHistoricalData(date: Date, cityCode: Int, stationId: Int) =
		mileageRepository.getHistoricalData(date, cityCode, stationId)

}
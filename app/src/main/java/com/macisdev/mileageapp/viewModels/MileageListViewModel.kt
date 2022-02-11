package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle

class MileageListViewModel(val plateNumber: String) : ViewModel() {
	private val mileageRepository = MileageRepository.get()
	val mileageListLiveData: LiveData<List<Mileage>> = mileageRepository.getMileages(plateNumber)

	private var deletedMileage: Mileage? = null
	private var deletedVehicle: Vehicle? = null
	private var clearedMileages: List<Mileage>? = null

	fun getVehicle(): LiveData<Vehicle> {
		return mileageRepository.getVehicle(plateNumber)
	}

	fun clearMileages(mileages: List<Mileage>) {
		clearedMileages = mileages
		mileageRepository.clearMileages(plateNumber)
	}

	fun deleteVehicle(vehicle: Vehicle, mileages: List<Mileage>) {
		clearedMileages = mileages
		deletedVehicle = vehicle
		mileageRepository.deleteVehicle(plateNumber)
	}

	fun deleteMileage(mileage: Mileage?) {
		deletedMileage = mileage
		mileageRepository.deleteMileage(mileage?.id)
	}

	fun restoreDeletedMileages() {
		deletedMileage?.let { mileageRepository.storeMileage(it) }
	}

	fun restoreClearedMileages() {
		clearedMileages?.forEach { mileageRepository.storeMileage(it) }
	}

	fun restoreDeletedVehicle() {
		deletedVehicle?.let { mileageRepository.addVehicle(it) }
		restoreClearedMileages()
	}

	fun storeMileage(mileage: Mileage) {
		mileageRepository.storeMileage(mileage)
	}

	@Suppress("UNCHECKED_CAST")
	class Factory(private val plateNumber : String) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			return MileageListViewModel(plateNumber) as T
		}
	}
}
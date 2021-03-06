package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.model.Vehicle

class MainActivityViewModel : ViewModel() {
	private val mileageRepository = MileageRepository.get()
	val vehiclesList: LiveData<List<Vehicle>> = mileageRepository.getVehicles()
}
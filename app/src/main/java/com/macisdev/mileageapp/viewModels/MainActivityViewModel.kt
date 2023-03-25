package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.database.AppDataRepository
import com.macisdev.mileageapp.model.Vehicle

class MainActivityViewModel : ViewModel() {
	private val appDataRepository = AppDataRepository.get()
	val vehiclesList: LiveData<List<Vehicle>> = appDataRepository.getVehicles()
}
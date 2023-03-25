package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.api.fuel.FuelStation
import com.macisdev.mileageapp.database.AppDataRepository

class FuelStationsFragmentViewModel : ViewModel() {
    private val appDataRepository = AppDataRepository.get()

    fun getCityCodeByZipCode(zip: String): LiveData<Int>
        = appDataRepository.getCityCodeByZipCode(zip)

    fun getFuelStationsByCityCode(cityCode: Int): LiveData<List<FuelStation>>
        = appDataRepository.getFuelStationsByCityCode(cityCode)
}
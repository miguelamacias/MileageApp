package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.api.fuel.ListaEESSPrecio
import com.macisdev.mileageapp.database.MileageRepository

class FuelStationsFragmentViewModel : ViewModel() {
    private val mileageRepository = MileageRepository.get()

    fun getCityCodeByZipCode(zip: String): LiveData<Int>
        = mileageRepository.getCityCodeByZipCode(zip)

    fun getFuelStationsByCityCode(cityCode: Int): LiveData<List<ListaEESSPrecio>>
        = mileageRepository.getFuelStationsByCityCode(cityCode)
}
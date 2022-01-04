package com.macisdev.mileageapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.macisdev.mileageapp.model.Mileage

class MileageListViewModel(private val plateNumber: String) : ViewModel() {
	val mileageListLiveData: MutableLiveData<List<Mileage>> = MileageRepository.getMileages(plateNumber)

	@Suppress("UNCHECKED_CAST")
	class Factory(private val plateNumber : String) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			return MileageListViewModel(plateNumber) as T
		}
	}
}

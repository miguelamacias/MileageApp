package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.utils.Utils
import java.util.*

class AddMileageViewModel : ViewModel() {
	private val milageRepository = MileageRepository.get()

	var date = Date()

	var formattedDate: String = ""
		get() {
			return Utils.formatDate(date)
		}
		private set

	fun storeMileage(mileage: Mileage) {
		milageRepository.storeMileage(mileage)
	}

	fun getMileage(id: String): LiveData<Mileage> = milageRepository.getMileage(UUID.fromString(id))

	fun updateMileage(mileage: Mileage) {
		milageRepository.updateMileage(mileage)
	}
}
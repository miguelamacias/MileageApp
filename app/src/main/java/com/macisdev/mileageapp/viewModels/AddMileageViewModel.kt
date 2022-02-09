package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.utils.Utils
import java.util.*

class AddMileageViewModel : ViewModel() {
	private val milageRepository = MileageRepository.get()

	var date = Date()

	var formatedDate: String = ""
		get() {
			return Utils.formatDate(date)
		}
		private set

	fun storeMileage(mileage: Mileage) {
		milageRepository.storeMileage(mileage)
	}
}
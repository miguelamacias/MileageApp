package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.model.Mileage
import java.text.SimpleDateFormat
import java.util.*

class AddMileageViewModel : ViewModel() {
	private val milageRepository = MileageRepository.get()
	private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

	var date = Date()
		set(value) {
			field = value
			formatedDate = dateFormat.format(date)
		}

	var formatedDate: String = dateFormat.format(date)
		private set

	fun storeMileage(mileage: Mileage) {
		milageRepository.storeMileage(mileage)
	}
}
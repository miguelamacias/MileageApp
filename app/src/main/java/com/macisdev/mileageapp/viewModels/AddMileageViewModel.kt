package com.macisdev.mileageapp.viewModels

import android.text.format.DateFormat
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.model.Mileage
import java.util.*

class AddMileageViewModel : ViewModel() {
	private val milageRepository = MileageRepository.get()

	var date = Date()

	var formatedDate: String = ""
		get() {
			return DateFormat.format(
				DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyy"),
			date).toString()
		}
		private set

	fun storeMileage(mileage: Mileage) {
		milageRepository.storeMileage(mileage)
	}
}
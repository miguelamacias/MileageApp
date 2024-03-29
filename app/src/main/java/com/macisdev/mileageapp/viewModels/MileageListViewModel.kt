package com.macisdev.mileageapp.viewModels

import android.text.format.DateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.macisdev.mileageapp.database.AppDataRepository
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class MileageListViewModel(val plateNumber: String) : ViewModel() {
	private val appDataRepository = AppDataRepository.get()
	val mileageListLiveData: LiveData<List<Mileage>> = appDataRepository.getMileages(plateNumber)

	private var deletedVehicle: Vehicle? = null
	private var deletedMileages: List<Mileage> = emptyList()
	private var unimportedLines = emptyList<String>()

	fun getVehicle(): LiveData<Vehicle> {
		return appDataRepository.getVehicle(plateNumber)
	}

	fun deleteVehicle(vehicle: Vehicle, mileages: List<Mileage>) {
		deletedMileages = mileages
		deletedVehicle = vehicle
		appDataRepository.deleteVehicle(plateNumber)
	}

	fun restoreDeletedMileages() {
		appDataRepository.storeMileages(deletedMileages)
	}

	fun deleteMileages(mileages: List<Mileage>) {
		deletedMileages = mileages
		appDataRepository.deleteMileages(mileages)
	}

	fun restoreDeletedVehicle() {
		deletedVehicle?.let { vehicle ->
			appDataRepository.restoreVehicle(vehicle, deletedMileages) }
	}

	private fun storeMileage(mileage: Mileage) {
		appDataRepository.storeMileage(mileage)
	}

	fun importCsvContent(csvFile: InputStream?): Int {
		val dateFormatter = SimpleDateFormat(
			DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyy"),
			Locale.getDefault()
		)
		val scanner = Scanner(csvFile)
		val parsedMileages = mutableListOf<Mileage>()

		var line: String
		var originalReadLine = ""
		var wrongLinesCount = 0
		var readLines = 0
		val wrongLinesContent: MutableList<String> = mutableListOf()
		if (scanner.hasNextLine()) {
			scanner.nextLine() //discards the header
		}

		while (scanner.hasNextLine()) {
			try {
				line = scanner.nextLine()
				originalReadLine = line

				if (line.count { it == ',' } == 5) {
					val id = UUID.randomUUID()

					val plateNumber = line.substringBefore(',')
					line = line.substringAfter(',')
					val date = dateFormatter.parse(line.substringBefore(','))
					line = line.substringAfter(',')
					val km = line.substringBefore(',').toDouble()
					line = line.substringAfter(',')
					val l = line.substringBefore(',').toDouble()
					line = line.substringAfter(',')
					val mileageValue = line.substringBefore(',').toDouble()
					val notes = line.substringAfter(',')

					if (plateNumber == this.plateNumber) {
						parsedMileages.add(Mileage(plateNumber, date ?: Date(), mileageValue, km, l, notes, id))
					} else {
						wrongLinesCount++
						wrongLinesContent.add(originalReadLine)
					}
				} else {
					wrongLinesCount++
					wrongLinesContent.add(originalReadLine)
				}
			} catch (e: Exception) {
				wrongLinesCount++
				wrongLinesContent.add(originalReadLine)
			} finally {
				readLines++
			}
		}

		parsedMileages.forEach {
			storeMileage(it)
		}

		unimportedLines = wrongLinesContent

		return when (wrongLinesCount) {
			0 -> {
				0
			}
			readLines -> {
				-1
			}
			else -> {
				wrongLinesCount
			}
		}
	}

	fun getUnimportedLines(): String {
		val unimportedLinesText = StringBuilder()
		unimportedLines.forEach {
			unimportedLinesText.append("> $it").append("\n")
		}

		return unimportedLinesText.toString().dropLast(1)
	}

	@Suppress("UNCHECKED_CAST")
	class Factory(private val plateNumber : String) : ViewModelProvider.Factory {
		override fun <T : ViewModel> create(modelClass: Class<T>): T {
			return MileageListViewModel(plateNumber) as T
		}
	}
}
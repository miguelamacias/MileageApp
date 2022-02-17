package com.macisdev.mileageapp.viewModels

import android.text.format.DateFormat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.macisdev.mileageapp.database.MileageRepository
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class MileageListViewModel(val plateNumber: String) : ViewModel() {
	private val mileageRepository = MileageRepository.get()
	val mileageListLiveData: LiveData<List<Mileage>> = mileageRepository.getMileages(plateNumber)

	private var deletedMileage: Mileage? = null
	private var deletedVehicle: Vehicle? = null
	private var clearedMileages: List<Mileage>? = null
	private var unimportedLines = emptyList<String>()

	fun getVehicle(): LiveData<Vehicle> {
		return mileageRepository.getVehicle(plateNumber)
	}

	fun clearMileages(mileages: List<Mileage>) {
		clearedMileages = mileages
		mileageRepository.clearMileages(plateNumber)
	}

	fun deleteVehicle(vehicle: Vehicle, mileages: List<Mileage>) {
		clearedMileages = mileages
		deletedVehicle = vehicle
		mileageRepository.deleteVehicle(plateNumber)
	}

	fun deleteMileage(mileage: Mileage?) {
		deletedMileage = mileage
		mileageRepository.deleteMileage(mileage?.id)
	}

	fun restoreDeletedMileages() {
		deletedMileage?.let { mileageRepository.storeMileage(it) }
	}

	fun restoreClearedMileages() {
		clearedMileages?.forEach { mileageRepository.storeMileage(it) }
	}

	fun restoreDeletedVehicle() {
		deletedVehicle?.let { viewModelScope.launch { mileageRepository.addVehicle(it) } }
		restoreClearedMileages()
	}

	private fun storeMileage(mileage: Mileage) {
		mileageRepository.storeMileage(mileage)
	}

	fun importCsvContent(csvFile: InputStream?): Int {
		val dateFormatter = SimpleDateFormat(
			DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyy"),
			Locale.getDefault()
		)
		val scanner = Scanner(csvFile)
		val parsedMileages = mutableListOf<Mileage>()

		var line: String
		var originalReadedLine = ""
		var wrongLinesCount = 0
		var readedLines = 0
		val wrongLinesContent: MutableList<String> = mutableListOf()
		if (scanner.hasNextLine()) {
			scanner.nextLine() //discards the header
		}

		while (scanner.hasNextLine()) {
			try {
				line = scanner.nextLine()
				originalReadedLine = line

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
						wrongLinesContent.add(originalReadedLine)
					}
				} else {
					wrongLinesCount++
					wrongLinesContent.add(originalReadedLine)
				}
			} catch (e: Exception) {
				wrongLinesCount++
				wrongLinesContent.add(originalReadedLine)
			} finally {
				readedLines++
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
			readedLines -> {
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
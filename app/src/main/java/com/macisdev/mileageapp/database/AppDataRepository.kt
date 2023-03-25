package com.macisdev.mileageapp.database

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.macisdev.mileageapp.api.FuelServiceImpl
import com.macisdev.mileageapp.api.MapsServiceImpl
import com.macisdev.mileageapp.api.fuel.FuelStation
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Note
import com.macisdev.mileageapp.model.Vehicle
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "mileage-database"

class AppDataRepository private constructor(val context: Context) {

	val database: MileagesDataBase = Room.databaseBuilder(
		context.applicationContext,
		MileagesDataBase::class.java,
		DATABASE_NAME
	)
		.build()

	private val mileageDao = database.mileageDao()
	private val vehicleDao = database.vehicleDao()
	private val noteDao = database.noteDao()
	private val executor = Executors.newSingleThreadExecutor()
	private val fuelService = FuelServiceImpl()
	private val mapsService = MapsServiceImpl()

	fun getVehicles() = vehicleDao.getVehicles()

	fun getMileage(id: UUID) = mileageDao.getMileage(id)

	fun getMileages(vehiclePlateNumber: String) = mileageDao.getMileages(vehiclePlateNumber)

	fun storeMileage(mileage: Mileage) {
		executor.execute { mileageDao.addMileage(mileage) }
	}

	fun restoreVehicle(vehicle: Vehicle, mileages: List<Mileage>) {
		executor.execute {
			vehicleDao.restoreVehicle(vehicle)
			mileages.forEach { mileageDao.addMileage(it) }
		}
	}

	suspend fun addVehicle(vehicle: Vehicle) = vehicleDao.addVehicle(vehicle)

	fun getVehicle(plateNumber: String) = vehicleDao.getVehicle(plateNumber)

	fun updateVehicle(vehicle: Vehicle) {
		executor.execute { vehicleDao.updateVehicle(vehicle) }
	}

	fun deleteVehicle(plateNumber: String) {
		executor.execute { vehicleDao.deleteVehicle(plateNumber) }
	}

	fun deleteMileages(mileageList: List<Mileage>) {
		executor.execute {
			mileageList.forEach { mileageDao.deleteMileage(it.id.toString()) }
		}
	}

	fun updateMileage(mileage: Mileage) {
		executor.execute { mileageDao.updateMileage(mileage) }
	}

	fun getStatistics() = mileageDao.getStatistics()

	fun getLastMileage() = mileageDao.getLastMileage()

	fun getVehicleAverageMileage(vehiclePlateNumber: String) = vehicleDao.getVehicleAverageMileage(vehiclePlateNumber)

	fun getCityCodeByZipCode(zip: String): LiveData<Int> = fuelService.getCityCodeByZipCode(zip)

	fun getFuelStationsByCityCode(cityCode: Int): LiveData<List<FuelStation>> =
		fuelService.getFuelStationsByCityCode(cityCode)

	fun getFuelStationById(cityCode: Int, stationId: Int): LiveData<FuelStation> =
		fuelService.getFuelStationById(cityCode, stationId)

	fun getTripDistance(origin: String, destination: String, avoidTolls: Boolean): LiveData<Double> =
		mapsService.getTripDistance(origin, destination, avoidTolls)

    fun getHistoricalData(date: Date, cityCode: Int, stationId: Int) =
		fuelService.getHistoricalData(date, cityCode, stationId)

	fun storeMileages(mileages: List<Mileage>) {
		mileages.forEach { storeMileage(it) }
	}

	fun addNote(note: Note) {
		executor.execute { noteDao.addNote(note) }
	}

	fun updateNote(note: Note) {
		executor.execute { noteDao.updateNote(note) }
	}

	fun getNotesByVehicle(vehiclePlateNumber: String) = noteDao.getNotesByVehicle(vehiclePlateNumber)

	fun getNotesByVehicleAndType(vehiclePlateNumber: String, type: String) =
		noteDao.getNotesByVehicleAndType(vehiclePlateNumber, type)

    var tripDuration = mapsService.tripDuration

	companion object {
		@SuppressLint("StaticFieldLeak")
		private var INSTANCE: AppDataRepository? = null

		fun initialize(context: Context) {
			if (INSTANCE == null) {
				INSTANCE = AppDataRepository(context)
			}
		}

		fun get(): AppDataRepository {
			return INSTANCE ?: throw IllegalStateException("MileageRepository must be initialized")
		}
	}
}
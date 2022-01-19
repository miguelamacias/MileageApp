package com.macisdev.mileageapp.database

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "mileage-database"

@SuppressLint("StaticFieldLeak")
class MileageRepository private constructor(context: Context) {

	private val database: MileagesDataBase = Room.databaseBuilder(
		context.applicationContext,
		MileagesDataBase::class.java,
		DATABASE_NAME)
		.build()

	private val mileageDao = database.mileageDao()
	private val executor = Executors.newSingleThreadExecutor()


	fun getVehicles() = mileageDao.getVehicles()

	fun getMileages(vehiclePlateNumber: String) = mileageDao.getMileages(vehiclePlateNumber)

	fun storeMileage(mileage: Mileage) {
		executor.execute { mileageDao.addMileage(mileage) }
	}

	fun addVehicle(vehicle: Vehicle) {
		executor.execute { mileageDao.addVehicle(vehicle) }
	}

	fun getVehicle(plateNumber: String) = mileageDao.getVehicle(plateNumber)

	fun updateVehicle(vehicle: Vehicle) {
		executor.execute { mileageDao.updateVehicle(vehicle)}
	}

	fun deleteVehicle(plateNumber: String) {
		executor.execute { mileageDao.deleteVehicle(plateNumber)}
	}

	fun clearMileages(vehiclePlateNumber: String) {
		executor.execute {mileageDao.clearMileages(vehiclePlateNumber)}
	}

	fun deleteMileage(mileageId: UUID?) {
		executor.execute {mileageDao.deleteMileage(mileageId.toString())}
	}

	fun getStatistics() = mileageDao.getStatistics()

	fun getLastMileage() = mileageDao.getLastMileage()

	companion object {
		private var INSTANCE: MileageRepository? = null

		fun initialize(context: Context) {
			if (INSTANCE == null) {
				INSTANCE = MileageRepository(context)
			}
		}

		fun get(): MileageRepository {
			return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
		}
	}
}
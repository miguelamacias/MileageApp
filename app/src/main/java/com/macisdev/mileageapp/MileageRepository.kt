package com.macisdev.mileageapp

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import com.macisdev.mileageapp.database.MileagesDataBase
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle

private const val DATABASE_NAME = "mileage-database"

@SuppressLint("StaticFieldLeak")
class MileageRepository private constructor(context: Context) {

	private lateinit var context: Context

	private val database: MileagesDataBase = Room.databaseBuilder(
		context.applicationContext,
		MileagesDataBase::class.java,
		DATABASE_NAME)
		.allowMainThreadQueries()
		.build()


	private val mileageDao = database.mileageDao()

	fun getVehicles() = mileageDao.getVehicles()

	fun storeMileage(mileage: Mileage) = mileageDao.addMileage(mileage)

	/*fun getMileages(plateNumber: String): MutableLiveData<List<Mileage>> {
		val plateMileages = mileages.filter { it.vehiclePlateNumber == plateNumber}
		return MutableLiveData<List<Mileage>>(plateMileages)
	}*/

	fun getMileages(vehiclePlateNumber: String) = mileageDao.getMileages(vehiclePlateNumber)

	fun addVehicle(vehicle: Vehicle) = mileageDao.addVehicle(vehicle)

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
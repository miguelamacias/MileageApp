package com.macisdev.mileageapp.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.macisdev.mileageapp.api.MapsServiceCalls
import com.macisdev.mileageapp.api.MatrixResponse
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "mileage-database"
const val API_STATUS_OK = "OK"
const val API_STATUS_ZERO_RESULTS = "ZERO_RESULTS"
const val TRIP_DISTANCE_ZERO_RESULTS_ERROR = -1.0

class MileageRepository private constructor(val context: Context) {

	val database: MileagesDataBase = Room.databaseBuilder(
		context.applicationContext,
		MileagesDataBase::class.java,
		DATABASE_NAME
	)
		.build()

	private val mileageDao = database.mileageDao()
	private val executor = Executors.newSingleThreadExecutor()

	fun getVehicles() = mileageDao.getVehicles()

	fun getMileages(vehiclePlateNumber: String) = mileageDao.getMileages(vehiclePlateNumber)

	fun storeMileage(mileage: Mileage) {
		executor.execute { mileageDao.addMileage(mileage) }
	}

	suspend fun addVehicle(vehicle: Vehicle) = mileageDao.addVehicle(vehicle)

	fun getVehicle(plateNumber: String) = mileageDao.getVehicle(plateNumber)

	fun updateVehicle(vehicle: Vehicle) {
		executor.execute { mileageDao.updateVehicle(vehicle) }
	}

	fun deleteVehicle(plateNumber: String) {
		executor.execute { mileageDao.deleteVehicle(plateNumber) }
	}

	fun clearMileages(vehiclePlateNumber: String) {
		executor.execute { mileageDao.clearMileages(vehiclePlateNumber) }
	}

	fun deleteMileage(mileageId: UUID?) {
		executor.execute { mileageDao.deleteMileage(mileageId.toString()) }
	}

	fun getStatistics() = mileageDao.getStatistics()

	fun getLastMileage() = mileageDao.getLastMileage()

	fun getVehicleAverageMileage(vehiclePlateNumber: String) = mileageDao.getVehicleAverageMileage(vehiclePlateNumber)

	var tripDuration = MutableLiveData<Int>()

	fun getTripDistance(origin: String, destination: String, avoidTolls: Boolean): LiveData<Double> {
		val responseLiveData: MutableLiveData<Double> = MutableLiveData()

		val retrofit: Retrofit = Retrofit.Builder()
			.baseUrl("https://maps.googleapis.com/maps/api/distancematrix/")
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		val mapsAPI = retrofit.create(MapsServiceCalls::class.java)

		val mapsRequest: Call<MatrixResponse> = if (avoidTolls) {
			mapsAPI.getDistanceAvoidTolls(origin, destination)
		} else {
			mapsAPI.getDistance(origin, destination)
		}

		mapsRequest.enqueue(object : Callback<MatrixResponse> {
			override fun onResponse(call: Call<MatrixResponse>, response: Response<MatrixResponse>) {
				val responseBody = response.body()
				val apiStatus = responseBody?.status
				val routeStatus =
					if (apiStatus == API_STATUS_OK) responseBody.rows.first().elements.first().status else ""

				if (apiStatus == API_STATUS_OK && routeStatus == API_STATUS_OK) {
					val tripDistance = responseBody
						.rows.first()
						.elements.first()
						.distance
						.value / 1000.0

					responseLiveData.value = tripDistance

					tripDuration.value = responseBody
						.rows.first()
						.elements.first()
						.duration
						.value / 60

				} else if (routeStatus == API_STATUS_ZERO_RESULTS) {
					responseLiveData.value = -1.0
				} else {
					responseLiveData.value = -2.0
				}
			}

			override fun onFailure(call: Call<MatrixResponse>, t: Throwable) {
				responseLiveData.value = -2.0
			}
		})
		return responseLiveData
	}

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
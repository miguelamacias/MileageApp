package com.macisdev.mileageapp.database

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Room
import com.macisdev.mileageapp.MainActivity
import com.macisdev.mileageapp.api.fuel.FuelResponse
import com.macisdev.mileageapp.api.fuel.FuelServiceCalls
import com.macisdev.mileageapp.api.fuel.ListaEESSPrecio
import com.macisdev.mileageapp.api.maps.MapsServiceCalls
import com.macisdev.mileageapp.api.maps.MatrixResponse
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

	fun getMileage(id: UUID) = mileageDao.getMileage(id)

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

	fun updateMileage(mileage: Mileage) {
		executor.execute { mileageDao.updateMileage(mileage) }
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

					Log.i(MainActivity.TAG, "Distance Matrix API called successfully.")
				} else if (routeStatus == API_STATUS_ZERO_RESULTS) {
					responseLiveData.value = -1.0
					Log.i(MainActivity.TAG, "Distance Matrix API called successfully, but no route was found.")
				} else {
					responseLiveData.value = -2.0
					Log.e(MainActivity.TAG, "Distance Matrix API call returned an error: $apiStatus")
				}
			}

			override fun onFailure(call: Call<MatrixResponse>, t: Throwable) {
				responseLiveData.value = -2.0
				Log.e(MainActivity.TAG, "Distance Matrix API call failed.")
			}
		})
		return responseLiveData
	}

	fun getCityCodeByZipCode(zip: String): LiveData<Int> {
		val cityCode: MutableLiveData<Int> = MutableLiveData()
		val retrofit: Retrofit = Retrofit.Builder()
			.baseUrl("https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/")
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		val fuelAPI = retrofit.create(FuelServiceCalls::class.java)

		fuelAPI.getAllFuelStations().enqueue(object : Callback<FuelResponse> {
			override fun onResponse(call: Call<FuelResponse>, response: Response<FuelResponse>) {
				val responseBody = response.body()
				var codeFound = false

				if (responseBody?.resultadoConsulta == API_STATUS_OK) {
					responseBody.listaEESSPrecio.takeWhile { !codeFound }.forEach { fuelStation ->
						if (fuelStation.CP.toString() == zip) {
							cityCode.value = fuelStation.iDMunicipio
							codeFound = true
						}
					}
				}
			}

			override fun onFailure(call: Call<FuelResponse>, t: Throwable) {
				Log.e(MainActivity.TAG, "ERROR CALLING FUEL SERVICE!")
			}

		})

		return cityCode
	}

	fun getFuelStationsByCityCode(cityCode: Int): LiveData<List<ListaEESSPrecio>> {
		val fuelStationsList: MutableLiveData<List<ListaEESSPrecio>> = MutableLiveData()
		val retrofit: Retrofit = Retrofit.Builder()
			.baseUrl("https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/")
			.addConverterFactory(GsonConverterFactory.create())
			.build()

		val fuelAPI = retrofit.create(FuelServiceCalls::class.java)

		fuelAPI.getByCity(cityCode).enqueue(object : Callback<FuelResponse> {
			override fun onResponse(call: Call<FuelResponse>, response: Response<FuelResponse>) {
				val responseBody = response.body()

				if (responseBody?.resultadoConsulta == API_STATUS_OK) {
					fuelStationsList.value = responseBody.listaEESSPrecio
				}
			}

			override fun onFailure(call: Call<FuelResponse>, t: Throwable) {
				Log.e(MainActivity.TAG, "ERROR CALLING FUEL SERVICE!")
			}

		})

		return fuelStationsList
	}

	companion object {
		@SuppressLint("StaticFieldLeak")
		private var INSTANCE: MileageRepository? = null

		fun initialize(context: Context) {
			if (INSTANCE == null) {
				INSTANCE = MileageRepository(context)
			}
		}

		fun get(): MileageRepository {
			return INSTANCE ?: throw IllegalStateException("MileageRepository must be initialized")
		}
	}
}
package com.macisdev.mileageapp.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.MainActivity
import com.macisdev.mileageapp.api.MapsServiceCalls
import com.macisdev.mileageapp.api.MatrixResponse
import com.macisdev.mileageapp.database.MileageRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TripCostViewModel : ViewModel() {
	companion object {
		const val API_STATUS_OK = "OK"
		const val API_STATUS_ZERO_RESULTS = "ZERO_RESULTS"
		const val TRIP_DISTANCE_ZERO_RESULTS_ERROR = -1.0
		const val TRIP_DISTANCE_GENERIC_ERROR = -1.0
	}

	var origin = ""
	var destination = ""
	private val mileageRepository = MileageRepository.get()

	fun getVehicles() = mileageRepository.getVehicles()

	fun getVehicleAverageMileage(plateNumber: String) = mileageRepository.getVehicleAverageMileage(plateNumber)

	fun getTripDistance(avoidTolls: Boolean): LiveData<Double> {
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
				val routeStatus = responseBody?.rows?.first()?.elements?.first()?.status

				if (apiStatus == "OK" && routeStatus == "OK") {
					val tripDistance = responseBody
						.rows.first()
						.elements.first()
						.distance
						.value / 1000.0

					responseLiveData.value = tripDistance
				} else if (routeStatus == "ZERO_RESULTS") {
					responseLiveData.value = -1.0
				} else {
					responseLiveData.value = -2.0
				}
			}

			override fun onFailure(call: Call<MatrixResponse>, t: Throwable) {
				Log.e(MainActivity.TAG, "ERROR!")
			}
		})
		return responseLiveData
	}
}
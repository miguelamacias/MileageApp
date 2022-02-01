package com.macisdev.mileageapp.viewModels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.MainActivity
import com.macisdev.mileageapp.api.MapsService
import com.macisdev.mileageapp.database.MileageRepository
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

class TripCostViewModel : ViewModel() {
	var origin = ""
	var destination = ""
	private val mileageRepository = MileageRepository.get()

	fun getVehicles() = mileageRepository.getVehicles()

	fun getVehicleAverageMileage(plateNumber: String) = mileageRepository.getVehicleAverageMileage(plateNumber)

	fun getTripDistance(avoidTolls: Boolean): LiveData<Double> {
		val responseLiveData: MutableLiveData<Double> = MutableLiveData()

		val retrofit: Retrofit = Retrofit.Builder()
			.baseUrl("https://maps.googleapis.com/maps/api/distancematrix/")
			.addConverterFactory(ScalarsConverterFactory.create())
			.build()

		val mapsAPI = retrofit.create(MapsService::class.java)

		val mapsRequest: Call<String> = if (avoidTolls) {
			mapsAPI.getDistanceAvoidTolls(origin, destination)
		} else {
			mapsAPI.getDistance(origin, destination)
		}

		mapsRequest.enqueue(object : Callback<String> {
			override fun onResponse(call: Call<String>, response: Response<String>) {
				val body = response.body().toString()
				Log.d(MainActivity.TAG, body)

				val jsonObject = JSONObject(body)

				val tripDistance = jsonObject
					.getJSONArray("rows")
					.getJSONObject(0)
					.getJSONArray("elements")
					.getJSONObject(0)
					.getJSONObject("distance")
					.getDouble("value") / 1000

				responseLiveData.value = tripDistance
			}

			override fun onFailure(call: Call<String>, t: Throwable) {
				Log.e(MainActivity.TAG, "ERROR!")
			}
		})
		return responseLiveData
	}
}
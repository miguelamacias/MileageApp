package com.macisdev.mileageapp.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macisdev.mileageapp.MainActivity
import com.macisdev.mileageapp.api.maps.MapsServiceCalls
import com.macisdev.mileageapp.api.maps.MatrixResponse
import com.macisdev.mileageapp.database.API_STATUS_OK
import com.macisdev.mileageapp.database.API_STATUS_ZERO_RESULTS
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsServiceImpl {
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
}
package com.macisdev.mileageapp.api.maps

import com.macisdev.mileageapp.BuildConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface MapsServiceCalls {
	@GET("json")
	fun getDistance(
		@Query("origins") origin: String,
		@Query("destinations") destination: String,
		@Query("key") key: String = BuildConfig.DISTANCE_MAPS_KEY
	) : Call<MatrixResponse>

	@GET("json")
	fun getDistanceAvoidTolls(
		@Query("origins") origin: String,
		@Query("destinations") destination: String,
		@Query("avoid") avoid: String = "tolls",
		@Query("key") key: String = BuildConfig.DISTANCE_MAPS_KEY
	) : Call<MatrixResponse>
}
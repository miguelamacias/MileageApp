package com.macisdev.mileageapp.api

import com.google.gson.annotations.SerializedName

data class Elements (

	@SerializedName("distance") val distance : Distance,
	@SerializedName("duration") val duration : Duration,
	@SerializedName("status") val status : String
)
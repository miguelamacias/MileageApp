package com.macisdev.mileageapp.api.maps

import com.google.gson.annotations.SerializedName

data class Duration (
	@SerializedName("text") val text : String,
	@SerializedName("value") val value : Int
)
package com.macisdev.mileageapp.api

import com.google.gson.annotations.SerializedName

data class MatrixResponse (

	@SerializedName("destination_addresses") val destination_addresses : List<String>,
	@SerializedName("origin_addresses") val origin_addresses : List<String>,
	@SerializedName("rows") val rows : List<Rows>,
	@SerializedName("status") val status : String
)
package com.macisdev.mileageapp.api.maps

import com.google.gson.annotations.SerializedName

data class Rows (
	@SerializedName("elements") val elements : List<Elements>
)
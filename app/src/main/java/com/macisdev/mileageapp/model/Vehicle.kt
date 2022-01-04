package com.macisdev.mileageapp.model

data class Vehicle(val plateNumber: String = "", val maker: String = "", val model: String = "") {
	override fun toString(): String {
		return "$maker $model - $plateNumber"
	}
}



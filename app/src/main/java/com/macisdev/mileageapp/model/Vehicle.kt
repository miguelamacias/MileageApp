package com.macisdev.mileageapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Vehicle(@PrimaryKey var plateNumber: String = "",
				   var maker: String = "",
				   var model: String = "",
				   var icon: String,
				   var color: String) {

	override fun toString(): String {
		return "$maker $model - $plateNumber"
	}
}



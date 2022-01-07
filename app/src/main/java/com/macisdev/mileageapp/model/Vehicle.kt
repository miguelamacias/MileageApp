package com.macisdev.mileageapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Vehicle(@PrimaryKey var plateNumber: String = "",
				   var maker: String = "",
				   var model: String = "",
				   var icon: Int,
				   var iconColor: Int) {

	override fun toString(): String {
		return "$maker $model - $plateNumber"
	}
}



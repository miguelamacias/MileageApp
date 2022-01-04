package com.macisdev.mileageapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Vehicle(@PrimaryKey val plateNumber: String = "",
				   val maker: String = "",
				   val model: String = "") {

	override fun toString(): String {
		return "$maker $model - $plateNumber"
	}
}



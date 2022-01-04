package com.macisdev.mileageapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import java.util.*

@Entity(
	foreignKeys = [ForeignKey(
		entity = Vehicle::class,
		parentColumns = arrayOf("plateNumber"),
		childColumns = arrayOf("vehiclePlateNumber"),
		onDelete = CASCADE
	)]
)
data class Mileage(
	@ColumnInfo(index = true) val vehiclePlateNumber: String,
	val date: Date,
	val mileage: Double,
	val kilometres: Double,
	val litres: Double,
	val notes: String = "",
	@PrimaryKey
	val id: UUID = UUID.randomUUID()
)

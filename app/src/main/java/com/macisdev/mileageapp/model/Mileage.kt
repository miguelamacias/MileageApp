package com.macisdev.mileageapp.model

import androidx.room.*
import androidx.room.ForeignKey.Companion.CASCADE
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
	var date: Date,
	var mileage: Double,
	var kilometres: Double,
	var litres: Double,
	var notes: String = "",
	@PrimaryKey
	val id: UUID = UUID.randomUUID(),
) {
	@Ignore
	var selectedInRecyclerView: Boolean = false
}

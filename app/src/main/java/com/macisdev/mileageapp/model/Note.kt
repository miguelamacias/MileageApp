package com.macisdev.mileageapp.model

import androidx.room.*
import java.util.*

@Entity(
    foreignKeys = [ForeignKey(
        entity = Vehicle::class,
        parentColumns = arrayOf("plateNumber"),
        childColumns = arrayOf("vehiclePlateNumber"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Note(
    @PrimaryKey
    val id: UUID,
    @ColumnInfo(index = true) val vehiclePlateNumber: String,
    var type: String,
    var title: String,
    var content: String,
    var date: Date = Date()
) {
    @Ignore
    var selectedInRecyclerView: Boolean = false

    companion object {
        const val TYPE_USER = "user"
        const val TYPE_INSPECTION = "inspection"
        const val TYPE_MAINTENANCE = "maintenance"
    }
}
package com.macisdev.mileageapp.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Note
import com.macisdev.mileageapp.model.Vehicle

@Database(entities = [Mileage::class, Vehicle::class, Note::class],
	version = 2,
	autoMigrations = [
		AutoMigration(from = 1, to = 2)
	]
)
@TypeConverters(DataBaseTypeConverters::class)
abstract class MileagesDataBase : RoomDatabase() {

	abstract fun mileageDao(): MileageDao

	abstract fun vehicleDao(): VehicleDao

	abstract fun noteDao(): NoteDao
}
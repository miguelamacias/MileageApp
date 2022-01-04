package com.macisdev.mileageapp.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle

@Database(entities = [Mileage::class, Vehicle::class], version = 1)
@TypeConverters(MileageTypeConverters::class)
abstract class MileagesDataBase : RoomDatabase() {

	abstract fun mileageDao(): MileageDao
}
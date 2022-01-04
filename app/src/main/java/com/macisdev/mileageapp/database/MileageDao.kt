package com.macisdev.mileageapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle

@Dao
interface MileageDao {

	@Query("SELECT * FROM mileage WHERE vehiclePlateNumber=(:vehiclePlateNumber)")
	fun getMileages(vehiclePlateNumber: String) : List<Mileage>

	@Query("SELECT * FROM vehicle")
	fun getVehicles() : List<Vehicle>

	@Insert
	fun addMileage(mileage: Mileage)

	@Insert
	fun addVehicle(vehicle: Vehicle)
}
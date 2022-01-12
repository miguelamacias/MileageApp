package com.macisdev.mileageapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Vehicle

@Dao
interface MileageDao {

	@Query("SELECT * FROM mileage WHERE vehiclePlateNumber=(:vehiclePlateNumber) ORDER BY date DESC")
	fun getMileages(vehiclePlateNumber: String) : LiveData<List<Mileage>>

	@Query("SELECT * FROM vehicle")
	fun getVehicles() : LiveData<List<Vehicle>>

	@Query("SELECT * FROM vehicle WHERE plateNumber =(:plateNumber)")
	fun getVehicle(plateNumber: String) : LiveData<Vehicle>

	@Update
	fun updateVehicle(vehicle: Vehicle)

	@Insert
	fun addMileage(mileage: Mileage)

	@Insert
	fun addVehicle(vehicle: Vehicle)

	@Query("DELETE FROM vehicle WHERE plateNumber=(:plateNumber)")
	fun deleteVehicle(plateNumber: String)

	@Query("DELETE FROM mileage WHERE vehiclePlateNumber=(:vehiclePlateNumber)")
	fun clearMileages(vehiclePlateNumber: String)
}
package com.macisdev.mileageapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.macisdev.mileageapp.model.Vehicle

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicle")
    fun getVehicles() : LiveData<List<Vehicle>>

    @Query("SELECT * FROM vehicle WHERE plateNumber = (:plateNumber)")
    fun getVehicle(plateNumber: String) : LiveData<Vehicle>

    @Update
    fun updateVehicle(vehicle: Vehicle)

    @Insert
    suspend fun addVehicle(vehicle: Vehicle)

    @Insert
    fun restoreVehicle(vehicle: Vehicle)

    @Query("DELETE FROM vehicle WHERE plateNumber = (:plateNumber)")
    fun deleteVehicle(plateNumber: String)

    @Query("SELECT avg(mileage) FROM mileage WHERE vehiclePlateNumber = (:vehiclePlateNumber)")
    fun getVehicleAverageMileage(vehiclePlateNumber: String): LiveData<Double>
}
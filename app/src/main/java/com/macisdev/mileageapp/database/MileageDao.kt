package com.macisdev.mileageapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Statistics
import com.macisdev.mileageapp.model.Vehicle
import java.util.*

@Dao
interface MileageDao {

	@Query("SELECT * FROM mileage WHERE vehiclePlateNumber=(:vehiclePlateNumber) ORDER BY date DESC")
	fun getMileages(vehiclePlateNumber: String) : LiveData<List<Mileage>>

	@Query("SELECT * FROM mileage WHERE id = :id")
	fun getMileage(id: UUID) : LiveData<Mileage>

	@Query("SELECT * FROM vehicle")
	fun getVehicles() : LiveData<List<Vehicle>>

	@Query("SELECT * FROM vehicle WHERE plateNumber =(:plateNumber)")
	fun getVehicle(plateNumber: String) : LiveData<Vehicle>

	@Update
	fun updateVehicle(vehicle: Vehicle)

	@Update
	fun updateMileage(mileage: Mileage)

	@Insert
	fun addMileage(mileage: Mileage)

	@Insert
	suspend fun addVehicle(vehicle: Vehicle)

	@Query("DELETE FROM vehicle WHERE plateNumber=(:plateNumber)")
	fun deleteVehicle(plateNumber: String)

	@Query("DELETE FROM mileage WHERE vehiclePlateNumber=(:vehiclePlateNumber)")
	fun clearMileages(vehiclePlateNumber: String)

	@Query("DELETE FROM mileage WHERE id=(:mileageId)")
	fun deleteMileage(mileageId: String)

	@Query("SELECT * FROM mileage WHERE date = (SELECT max(date) FROM mileage)")
	fun getLastMileage() : LiveData<Mileage>

	@Query("SELECT count(*) AS totalRecords, avg(mileage) AS  averageMileage," +
			"sum(litres) AS totalLitres, sum(kilometres) AS totalKilometres FROM Mileage")
	fun getStatistics(): LiveData<Statistics>

	@Query("SELECT avg(mileage) FROM mileage WHERE vehiclePlateNumber=(:vehiclePlateNumber)")
	fun getVehicleAverageMileage(vehiclePlateNumber: String): LiveData<Double>
}
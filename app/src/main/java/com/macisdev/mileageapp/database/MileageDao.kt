package com.macisdev.mileageapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Statistics
import java.util.*

@Dao
interface MileageDao {

	@Query("SELECT * FROM mileage WHERE vehiclePlateNumber=(:vehiclePlateNumber) ORDER BY date DESC")
	fun getMileages(vehiclePlateNumber: String) : LiveData<List<Mileage>>

	@Query("SELECT * FROM mileage WHERE id = :id")
	fun getMileage(id: UUID) : LiveData<Mileage>

	@Update
	fun updateMileage(mileage: Mileage)

	@Insert
	fun addMileage(mileage: Mileage)

	@Query("DELETE FROM mileage WHERE id=(:mileageId)")
	fun deleteMileage(mileageId: String)

	@Query("SELECT * FROM mileage WHERE date = (SELECT max(date) FROM mileage)")
	fun getLastMileage() : LiveData<Mileage>

	@Query("SELECT count(*) AS totalRecords, avg(mileage) AS averageMileage," +
			"sum(litres) AS totalLitres, sum(kilometres) AS totalKilometres FROM Mileage")
	fun getStatistics(): LiveData<Statistics>


}
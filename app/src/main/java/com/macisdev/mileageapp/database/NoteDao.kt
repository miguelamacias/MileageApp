package com.macisdev.mileageapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.macisdev.mileageapp.model.Note

@Dao
interface NoteDao {

    @Query("SELECT * FROM Note WHERE id = (:id)")
    fun getNote(id: String): LiveData<Note>

    @Query("SELECT * FROM Note WHERE vehiclePlateNumber = (:vehiclePlateNumber) ORDER BY date DESC")
    fun getNotesByVehicle(vehiclePlateNumber: String) : LiveData<List<Note>>

    @Query("SELECT * FROM Note WHERE vehiclePlateNumber = (:vehiclePlateNumber) AND type = (:type) ORDER BY date DESC")
    fun getNotesByVehicleAndType(vehiclePlateNumber: String, type: String) : LiveData<List<Note>>

    @Insert
    fun addNote(note: Note)

    @Update
    fun updateNote(note: Note)
}
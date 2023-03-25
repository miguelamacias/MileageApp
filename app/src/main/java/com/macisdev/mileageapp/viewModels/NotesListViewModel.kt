package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.macisdev.mileageapp.database.AppDataRepository
import com.macisdev.mileageapp.model.Note
import java.util.*

class NotesListViewModel(val plateNumber: String) : ViewModel() {
    private val appDataRepository = AppDataRepository.get()

    val userNotesListLiveData =
        appDataRepository.getNotesByVehicleAndType(plateNumber, Note.TYPE_USER)

    val inspectionNotesLiveData =
        appDataRepository.getNotesByVehicleAndType(plateNumber, Note.TYPE_INSPECTION)

    val maintenanceNotesLiveData =
        appDataRepository.getNotesByVehicleAndType(plateNumber, Note.TYPE_MAINTENANCE)


    @Suppress("UNCHECKED_CAST")
    class Factory(private val plateNumber : String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesListViewModel(plateNumber) as T
        }
    }
}
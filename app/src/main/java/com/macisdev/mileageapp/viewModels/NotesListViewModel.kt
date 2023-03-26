package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.macisdev.mileageapp.NotesListFragmentDirections
import com.macisdev.mileageapp.database.AppDataRepository
import com.macisdev.mileageapp.model.Note
import java.util.*

class NotesListViewModel(val plateNumber: String) : ViewModel() {
    private val appDataRepository = AppDataRepository.get()
    private var deletedNotes: List<Note> = emptyList()

    fun getUserNotesListLiveData() =
        appDataRepository.getNotesByVehicleAndType(plateNumber, Note.TYPE_USER)

    fun getInspectionNotesLiveData() =
        appDataRepository.getNotesByVehicleAndType(plateNumber, Note.TYPE_INSPECTION)

    fun getMaintenanceNotesLiveData() =
        appDataRepository.getNotesByVehicleAndType(plateNumber, Note.TYPE_MAINTENANCE)


    fun getAddUserNoteDirections() = NotesListFragmentDirections
        .actionNotesListFragmentToAddNoteFragment(plateNumber)
    fun getAddInspectionNoteDirections() = NotesListFragmentDirections
    .actionNotesListFragmentToAddNoteFragment(plateNumber, noteType = Note.TYPE_INSPECTION)
    fun getAddMaintenanceNoteDirections() = NotesListFragmentDirections
        .actionNotesListFragmentToAddNoteFragment(plateNumber, noteType = Note.TYPE_MAINTENANCE)
    fun getEditNoteDirections(noteId: String) = NotesListFragmentDirections
        .actionNotesListFragmentToAddNoteFragment(plateNumber, editMode = true, noteId = noteId)

    fun deleteNotes(notes: List<Note>) {
        deletedNotes = notes
        appDataRepository.deleteNotes(notes)
    }

    fun restoreDeletedNotes() {
        deletedNotes.forEach { appDataRepository.addNote(it) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val plateNumber : String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesListViewModel(plateNumber) as T
        }
    }
}
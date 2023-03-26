package com.macisdev.mileageapp.viewModels

import androidx.lifecycle.ViewModel
import com.macisdev.mileageapp.database.AppDataRepository
import com.macisdev.mileageapp.model.Note

class AddNoteViewModel : ViewModel() {
    private val appDataRepository = AppDataRepository.get()

    fun addNote(note: Note) = appDataRepository.addNote(note)

    fun getNote(id: String) = appDataRepository.getNote(id)
}
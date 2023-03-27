package com.macisdev.mileageapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.macisdev.mileageapp.databinding.FragmentAddNoteBinding
import com.macisdev.mileageapp.model.Note
import com.macisdev.mileageapp.utils.Utils
import com.macisdev.mileageapp.viewModels.AddNoteViewModel
import java.util.*

class AddNoteFragment : BottomSheetDialogFragment() {

    private val addNoteVM: AddNoteViewModel by viewModels()
    private val fragmentArgs: AddNoteFragmentArgs by navArgs()
    private lateinit var gui: FragmentAddNoteBinding
    private lateinit var editNoteDate: Date


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.MileageDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        gui = FragmentAddNoteBinding.inflate(inflater, container, false)
        return gui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (fragmentArgs.editMode) {
            addNoteVM.getNote(fragmentArgs.noteId).observe(viewLifecycleOwner) { note ->
                gui.noteTitleEditText.setText(note.title)
                gui.noteContentEditText.setText(note.content)
                editNoteDate = note.date

                updateGUI(note.type, true)

            }
        } else {
            updateGUI(fragmentArgs.noteType, false)
        }

        gui.saveNoteButton.setOnClickListener {
            saveNote()
        }
    }

    private fun updateGUI(noteType: String, edit: Boolean) {
        when (noteType) {
            Note.TYPE_INSPECTION -> {
                val title = if (!edit) R.string.add_inspection else R.string.edit_inspection
                gui.addNotesFragmentTitle.text = getString(title)
                gui.noteTitleEditText.hint = getString(R.string.date_inspection)
                gui.noteContentEditText.hint = getString(R.string.notes_hint)
                gui.noteTitleEditText.isFocusable = false

                gui.noteTitleEditText.setOnClickListener {
                    showDatePicker()
                }
            }

            Note.TYPE_MAINTENANCE -> {
                val title = if (!edit) R.string.add_maintenance else R.string.edit_maintenance
                gui.addNotesFragmentTitle.text = getString(title)
                gui.noteTitleEditText.hint = getString(R.string.next_maintenance_at)
                gui.noteContentEditText.hint = getString(R.string.notes_hint)
            }

            Note.TYPE_USER -> if (edit) gui.addNotesFragmentTitle.text = getString(R.string.edit_note)
        }
    }

    private fun showDatePicker() {
        val currentDate = Calendar.getInstance()
        val currentYear = currentDate[Calendar.YEAR]
        val currentMonth = currentDate[Calendar.MONTH]
        val currentDay = currentDate[Calendar.DAY_OF_MONTH]

        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->

                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, selectedYear)
                selectedDate.set(Calendar.MONTH, selectedMonth)
                selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay)

                gui.noteTitleEditText.setText(Utils.formatDate(selectedDate.time))

            }, currentYear, currentMonth, currentDay
        )
        datePicker.show()

    }

    private fun saveNote() {
        val title = gui.noteTitleEditText.text.toString()
        val content = gui.noteContentEditText.text.toString()
        val id = if (!fragmentArgs.editMode) {
            UUID.randomUUID()
        } else {
            UUID.fromString(fragmentArgs.noteId)
        }

        if (!fragmentArgs.editMode) {
            addNoteVM.addNote(Note(id, fragmentArgs.plateNumber, fragmentArgs.noteType, title, content))
        } else {
            addNoteVM.updateNote(
                Note(id, fragmentArgs.plateNumber, fragmentArgs.noteType, title, content, editNoteDate)
            )
        }

        findNavController().navigateUp()
    }
}
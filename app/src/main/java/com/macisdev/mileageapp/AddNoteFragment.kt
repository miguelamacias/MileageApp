package com.macisdev.mileageapp

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
import com.macisdev.mileageapp.viewModels.AddNoteViewModel
import java.util.*

class AddNoteFragment : BottomSheetDialogFragment() {

    private val addNoteVM: AddNoteViewModel by viewModels()
    private val fragmentArgs: AddNoteFragmentArgs by navArgs()
    private lateinit var gui: FragmentAddNoteBinding


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

                gui.addNotesFragmentTitle.text = when (note.type) {
                    Note.TYPE_INSPECTION -> {
                        getString(R.string.edit_inspection)
                    }
                    Note.TYPE_MAINTENANCE -> {
                        getString(R.string.edit_maintenance)
                    }
                    else -> {
                        getString(R.string.edit_note)
                    }
                }
            }
        } else {
            if(fragmentArgs.noteType == Note.TYPE_INSPECTION) {
                gui.addNotesFragmentTitle.text = getString(R.string.add_inspection)
                gui.noteTitleEditText.hint = getString(R.string.date_inspection)
                gui.noteContentEditText.hint = getString(R.string.notes_hint)

            } else if (fragmentArgs.noteType == Note.TYPE_MAINTENANCE) {
                gui.addNotesFragmentTitle.text = getString(R.string.add_maintenance)
                gui.noteTitleEditText.hint = getString(R.string.next_maintenance_at)
                gui.noteContentEditText.hint = getString(R.string.notes_hint)
            }
        }

        gui.saveNoteButton.setOnClickListener {
            addNote()
        }
    }

    private fun addNote() {
        val title = gui.noteTitleEditText.text.toString()
        val content = gui.noteContentEditText.text.toString()
        val id = if (!fragmentArgs.editMode) {
            UUID.randomUUID()
        } else {
            UUID.fromString(fragmentArgs.noteId)
        }

        addNoteVM.addNote(Note(id, fragmentArgs.plateNumber, fragmentArgs.noteType, title, content))
        findNavController().navigateUp()
    }
}
package com.macisdev.mileageapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.macisdev.mileageapp.databinding.FragmentNotesListBinding
import com.macisdev.mileageapp.model.Note
import com.macisdev.mileageapp.utils.Utils
import com.macisdev.mileageapp.viewModels.NotesListViewModel
import java.util.*

class NotesListFragment : Fragment() {
    private lateinit var gui: FragmentNotesListBinding

    private val fragmentArgs: NotesListFragmentArgs by navArgs()
    private val notesListVM: NotesListViewModel by viewModels {
        NotesListViewModel.Factory(fragmentArgs.vehiclePlateNumber)
    }
    private var actionMode: ActionMode? = null
    private val vehiclePlateNumber by lazy { fragmentArgs.vehiclePlateNumber }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        gui = FragmentNotesListBinding.inflate(inflater, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.notes_bar_title, vehiclePlateNumber)

        return gui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var inspectionNote: Note? = null
        var maintenanceNote: Note? = null

        notesListVM.getInspectionNotesLiveData().observe(viewLifecycleOwner) { inspectionNotes ->
            inspectionNote = inspectionNotes.maxByOrNull { note -> note.date }
            gui.nextInspectionText.setText(inspectionNote?.content)
        }

        notesListVM.getMaintenanceNotesLiveData().observe(viewLifecycleOwner) { maintenanceNotes ->
            maintenanceNote = maintenanceNotes.maxByOrNull { note -> note.date }
            gui.nextMaintenanceText.setText(maintenanceNote?.content)
        }

        notesListVM.getUserNotesListLiveData().observe(viewLifecycleOwner) { updateNotes(it) }

        gui.notesRecyclerView.layoutManager = LinearLayoutManager(view.context)
        val adapter = NotesAdapter()
        gui.notesRecyclerView.adapter = adapter

        gui.addNoteFab.setOnClickListener {
            findNavController().navigate(notesListVM.getAddUserNoteDirections())
        }

        gui.addInspectionBtn.setOnClickListener {
            findNavController().navigate(notesListVM.getAddInspectionNoteDirections())
        }

        gui.addMaintenanceBtn.setOnClickListener {
            findNavController().navigate(notesListVM.getAddMaintenanceNoteDirections())
        }

        gui.nextInspectionText.setOnClickListener {
            findNavController().navigate(notesListVM.getEditNoteDirections(inspectionNote?.id.toString()))
        }

        gui.nextMaintenanceText.setOnClickListener {
            findNavController().navigate(notesListVM.getEditNoteDirections(maintenanceNote?.id.toString()))
        }
    }

    private fun updateNotes(notes: List<Note>) {
        val adapter = gui.notesRecyclerView.adapter as NotesListFragment.NotesAdapter
        adapter.submitList(notes)

        if (notes.isEmpty()) {
            gui.noNotesTextView.visibility = View.VISIBLE
        } else {
            gui.noNotesTextView.visibility = View.INVISIBLE
        }
    }

    private fun deleteNotes(mileages: List<Note>, warningText: Int, successText: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle(warningText)
            .setMessage(R.string.action_cannot_be_undone)
            .setPositiveButton(R.string.accept) { _, _ ->
                notesListVM.deleteNotes(mileages)
                Snackbar
                    .make(gui.coordinatorLayout, successText, Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo) { notesListVM.restoreDeletedNotes() }
                    .show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private inner class NotesAdapter : ListAdapter<Note, NotesAdapter.NotesViewHolder>(NotesDiffCallback) {

        private inner class NotesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private var currentNote: Note? = null

            private val titleTextView: TextView = view.findViewById(R.id.note_title)
            private val dateTextView: TextView = view.findViewById(R.id.note_date)
            private val contentTextView: TextView = view.findViewById(R.id.note_content)

            private val actionModeCallback = object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                    menu.clear()
                    if (currentList.count { it.selectedInRecyclerView } > 1) {
                        mode.menuInflater.inflate(R.menu.menu_contextual_note_multi_selection, menu)
                    } else {
                        mode.menuInflater.inflate(R.menu.menu_contextual_note_one_selection, menu)
                    }
                    return true
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                    return when (item.itemId) {
                        R.id.edit_note -> {
                            findNavController().navigate(notesListVM.getEditNoteDirections(currentNote?.id.toString()))
                            actionMode?.finish()
                            true
                        }

                        R.id.delete_note -> {
                            val selectedNotes = currentList.filter { it.selectedInRecyclerView }
                            deleteNotes(selectedNotes, R.string.delete_selected_notes, R.string.selected_notes_deleted)
                            actionMode?.finish()
                            true
                        }

                        R.id.select_all -> {
                            currentList.forEach { it.selectedInRecyclerView = true}
                            notifyDataSetChanged()
                            actionMode?.invalidate()
                            actionMode?.title = currentList.count { it.selectedInRecyclerView }.toString()
                            true
                        }
                        else -> false
                    }
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun onDestroyActionMode(mode: ActionMode) {
                    currentList.forEach { it.selectedInRecyclerView = false }
                    notifyDataSetChanged()
                    actionMode = null
                }
            }

            init {
                itemView.setOnLongClickListener {
                    currentNote?.selectedInRecyclerView = true
                    val itemPosition = gui.notesRecyclerView.getChildLayoutPosition(view)
                    notifyItemChanged(itemPosition)

                    if (actionMode == null) {
                        actionMode = requireActivity().startActionMode(actionModeCallback)
                        actionMode?.title = currentList.count { it.selectedInRecyclerView }.toString()
                        true
                    }
                    else false
                }

                itemView.setOnClickListener {
                    if (actionMode != null) {
                        currentNote?.apply{ selectedInRecyclerView = !selectedInRecyclerView}
                        val itemPosition = gui.notesRecyclerView.getChildLayoutPosition(view)
                        notifyItemChanged(itemPosition)
                        actionMode?.invalidate()
                        actionMode?.title = currentList.count { it.selectedInRecyclerView }.toString()
                        if (currentList.none { it.selectedInRecyclerView }) {
                            actionMode?.finish()
                        }
                    }
                }
            }

            fun bindData(note: Note) {
                currentNote = note

                titleTextView.text = note.title
                dateTextView.text = Utils.formatDate(note.date)
                contentTextView.text = note.content
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesAdapter.NotesViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return NotesViewHolder(layoutInflater.inflate(R.layout.list_item_note, parent, false))
        }

        override fun onBindViewHolder(holder: NotesAdapter.NotesViewHolder, position: Int) {
            holder.bindData(currentList[position])
            val cardView = holder.itemView as CardView
            if (currentList[position].selectedInRecyclerView) {
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary_light))
            } else {
                cardView.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }

        override fun onCurrentListChanged(previousList: MutableList<Note>, currentList: MutableList<Note>) {
            super.onCurrentListChanged(previousList, currentList)
            if (currentList.size == previousList.size + 1) {
                gui.notesRecyclerView.scrollToPosition(0)
            }
        }
    }

    private object NotesDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}
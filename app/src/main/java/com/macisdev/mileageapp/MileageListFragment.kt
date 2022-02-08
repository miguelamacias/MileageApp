package com.macisdev.mileageapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.macisdev.mileageapp.databinding.FragmentMileageListBinding
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.viewModels.MileageListViewModel
import java.util.*

class MileageListFragment : Fragment() {
	private lateinit var gui: FragmentMileageListBinding

	private val fragmentArgs: MileageListFragmentArgs by navArgs()
	private val mileageListVM: MileageListViewModel by viewModels {
		MileageListViewModel.Factory(fragmentArgs.vehiclePlateNumber)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentMileageListBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		mileageListVM.mileageListLiveData.observe(viewLifecycleOwner) {updateMileages(it)}

		gui.mileagesRecyclerView.layoutManager = LinearLayoutManager(view.context)
		val adapter = MileageAdapter()
		gui.mileagesRecyclerView.adapter = adapter

		gui.addMileageFab.setOnClickListener {
			val directions = MileageListFragmentDirections
				.actionMileageListFragmentToAddMileageFragment(mileageListVM.plateNumber)
			findNavController().navigate(directions)
		}
	}

	@SuppressLint("RestrictedApi")
	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater.inflate(R.menu.menu_list_mileage, menu)
		if (menu is MenuBuilder) menu.setOptionalIconsVisible(true)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.edit_vehicle -> {
				val directions = MileageListFragmentDirections
					.actionMileageListFragmentToAddVehicleFragment(true, mileageListVM.plateNumber)
				findNavController().navigate(directions)
				true
			}
			R.id.clear_mileages -> {
				clearMileagesItemSelected()
				true
			}
			R.id.delete_vehicle -> {
				deleteVehicleItemSelected()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun clearMileagesItemSelected() {
		val adapter = gui.mileagesRecyclerView.adapter as MileageAdapter

		AlertDialog.Builder(requireContext())
			.setTitle(R.string.clear_mileages)
			.setMessage(R.string.action_cannot_be_undone)
			.setPositiveButton(R.string.accept) { _, _ ->
				mileageListVM.clearMileages(adapter.currentList)
				Snackbar
					.make(gui.coordinatorLayout, R.string.cleared_mileages, Snackbar.LENGTH_LONG)
					.setAction(R.string.undo) { mileageListVM.restoreClearedMileages() }
					.show()
			}
			.setNegativeButton(R.string.cancel) {_, _ -> } //Nothing to do here
			.show()
	}

	private fun deleteVehicleItemSelected() {
		val adapter = gui.mileagesRecyclerView.adapter as MileageAdapter
		AlertDialog.Builder(requireContext())
			.setTitle(R.string.delete_vehicle)
			.setMessage(R.string.action_cannot_be_undone)
			.setPositiveButton(R.string.accept) { _, _ ->
				mileageListVM.getVehicle().observe(viewLifecycleOwner) {
					mileageListVM.deleteVehicle(it, adapter.currentList)
				}

				Snackbar
					.make(gui.root, R.string.deleted_vehicle, Snackbar.LENGTH_LONG)
					.setAction(R.string.undo) { mileageListVM.restoreDeletedVehicle()}
					.show()

				findNavController().navigateUp()
			}
			.setNegativeButton(R.string.cancel) {_, _ -> } //Nothing to do here
			.show()
	}

	private fun updateMileages(mileages: List<Mileage>) {
		val adapter = gui.mileagesRecyclerView.adapter as MileageAdapter
		adapter.submitList(mileages)
		gui.recordsCountTextView.text = mileages.size.toString()
		gui.averageMileageTextView.text = calculateAverageMileage(mileages)


		if (mileages.isEmpty()) {
			gui.noMileagesTextView.visibility = View.VISIBLE
		} else {
			gui.noMileagesTextView.visibility = View.INVISIBLE
		}
	}

	private fun calculateAverageMileage(mileages: List<Mileage>) = String.format(Locale.getDefault(), "%.2f",
		mileages.sumOf { it.mileage } / mileages.size)


	private inner class MileageAdapter : ListAdapter<Mileage, MileageAdapter.MileageViewHolder>(MileageDiffCallback) {

		private inner class MileageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
			private var currentMileage: Mileage? = null
			private val mileageContentTextView: TextView = view.findViewById(R.id.mileage_content_text_view)
			private val dateContentTextView: TextView = view.findViewById(R.id.date_content_text_view)
			private val kilometresContentTextView: TextView = view.findViewById(R.id.kilometres_content_text_view)
			private val litresContentTextView: TextView = view.findViewById(R.id.litres_content_text_view)
			private val notesTextView: TextView = view.findViewById(R.id.notes_text_view)

			init {
				itemView.setOnLongClickListener {
					val popup = PopupMenu(view.context, litresContentTextView)
					popup.inflate(R.menu.menu_popup_mileage)
					popup.setOnMenuItemClickListener {
						mileageListVM.deleteMileage(currentMileage)
						Snackbar
							.make(gui.coordinatorLayout, R.string.deleted_mileage, Snackbar.LENGTH_LONG)
							.setAction(R.string.undo) { mileageListVM.restoreDeletedMileages() }
							.show()
						true
					}
					popup.show()
					true
				}

				notesTextView.setOnClickListener {
					val notesVisibleText = notesTextView.layout.text

					if (notesVisibleText != currentMileage?.notes) {
						Toast.makeText(requireContext(), currentMileage?.notes, Toast.LENGTH_LONG).show()
					}
				}
			}

			fun bindData(mileage: Mileage) {
				currentMileage = mileage

				mileageContentTextView.text = String.format(Locale.getDefault(), "%.2f", mileage.mileage)
				dateContentTextView.text = DateFormat.format(
					DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyy"),
					mileage.date).toString()
				kilometresContentTextView.text = String.format(Locale.getDefault(), "%.2f", mileage.kilometres)
				litresContentTextView.text = String.format(Locale.getDefault(), "%.2f", mileage.litres)
				notesTextView.text = mileage.notes
			}
		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MileageAdapter.MileageViewHolder {
			val layoutInflater = LayoutInflater.from(parent.context)
			return MileageViewHolder(layoutInflater.inflate(R.layout.list_item_mileage, parent, false))
		}

		override fun onBindViewHolder(holder: MileageAdapter.MileageViewHolder, position: Int) {
			holder.bindData(currentList[position])
		}

		override fun onCurrentListChanged(previousList: MutableList<Mileage>, currentList: MutableList<Mileage>) {
			super.onCurrentListChanged(previousList, currentList)
			if (currentList.size == previousList.size + 1) {
				gui.mileagesRecyclerView.scrollToPosition(0)
			}
		}
	}

	private object MileageDiffCallback : DiffUtil.ItemCallback<Mileage>() {
		override fun areItemsTheSame(oldItem: Mileage, newItem: Mileage): Boolean {
			return oldItem.id == newItem.id
		}

		override fun areContentsTheSame(oldItem: Mileage, newItem: Mileage): Boolean {
			return oldItem == newItem
		}
	}
}
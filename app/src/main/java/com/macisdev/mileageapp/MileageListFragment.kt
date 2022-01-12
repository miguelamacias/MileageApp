package com.macisdev.mileageapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.macisdev.mileageapp.databinding.FragmentMileageListBinding
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.utils.Adapters
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
		val adapter = Adapters.MileageAdapter(gui.root)
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
				AlertDialog.Builder(requireContext())
					.setTitle(R.string.clear_mileages)
					.setMessage(R.string.action_cannot_be_undone)
					.setPositiveButton(R.string.accept) { _, _ ->
						mileageListVM.clearMileages()
						Snackbar.make(gui.root, R.string.cleared_mileages, Snackbar.LENGTH_LONG).show()
					}
					.setNegativeButton(R.string.cancel) {_, _ -> } //Nothing to do here
					.show()
				true
			}

			R.id.delete_vehicle -> {
				AlertDialog.Builder(requireContext())
					.setTitle(R.string.delete_vehicle)
					.setMessage(R.string.action_cannot_be_undone)
					.setPositiveButton(R.string.accept) { _, _ ->
						mileageListVM.deleteVehicle()
						Snackbar.make(gui.root, R.string.deleted_vehicle, Snackbar.LENGTH_LONG).show()

						findNavController().navigateUp()
					}
					.setNegativeButton(R.string.cancel) {_, _ -> } //Nothing to do here
					.show()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun updateMileages(mileages: List<Mileage>) {
		val adapter = gui.mileagesRecyclerView.adapter as Adapters.MileageAdapter
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

}
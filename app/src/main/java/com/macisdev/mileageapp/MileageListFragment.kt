package com.macisdev.mileageapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.format.DateFormat
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import com.macisdev.mileageapp.utils.Utils
import com.macisdev.mileageapp.utils.showToast
import com.macisdev.mileageapp.viewModels.MileageListViewModel
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class MileageListFragment : Fragment() {
	private lateinit var gui: FragmentMileageListBinding

	private val fragmentArgs: MileageListFragmentArgs by navArgs()
	private val mileageListVM: MileageListViewModel by viewModels {
		MileageListViewModel.Factory(fragmentArgs.vehiclePlateNumber)
	}
	private lateinit var importCsvLauncher: ActivityResultLauncher<Intent>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)

		importCsvLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			//Import CSV here!
			result?.data?.data?.let { uri ->
				val inputStream = requireContext().contentResolver.openInputStream(uri)
				importCsvContent(inputStream)
			}
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentMileageListBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		mileageListVM.mileageListLiveData.observe(viewLifecycleOwner) { updateMileages(it) }

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
			R.id.export_csv -> {
				exportCsvFile(mileageListVM.mileageListLiveData.value ?: emptyList())
				true
			}
			R.id.import_csv -> {
				openCsvFile()
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}

	private fun openCsvFile() {
		var chooseFile = Intent(Intent.ACTION_GET_CONTENT).apply {
			type = "text/*"
			addCategory(Intent.CATEGORY_OPENABLE)
		}

		chooseFile = Intent(Intent.createChooser(chooseFile, "Choose a file!"))
		importCsvLauncher.launch(chooseFile)
	}

	private fun importCsvContent(csvFile: InputStream?) {
		val dateFormatter = SimpleDateFormat(
			DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyy"),
			Locale.getDefault()
		)
		val scanner = Scanner(csvFile)
		val parsedMileages = mutableListOf<Mileage>()


		try {
			var line = scanner.nextLine() //discards the header

			while (scanner.hasNextLine()) {
				line = scanner.nextLine()
				val id = UUID.randomUUID()

				line = line.substringAfter(',')
				val plateNumber = line.substringBefore(',')
				line = line.substringAfter(',')
				val date = dateFormatter.parse(line.substringBefore(','))
				line = line.substringAfter(',')
				val km = line.substringBefore(',').toDouble()
				line = line.substringAfter(',')
				val l = line.substringBefore(',').toDouble()
				line = line.substringAfter(',')
				val mileageValue = line.substringBefore(',').toDouble()
				val notes = line.substringAfter(',')


				parsedMileages.add(Mileage(plateNumber, date ?: Date(), mileageValue, km, l, notes, id))
			}

			val correspondingMileages = parsedMileages.filter { it.vehiclePlateNumber == mileageListVM.plateNumber }

			if (correspondingMileages.size == parsedMileages.size) {
				correspondingMileages.forEach {
					mileageListVM.storeMileage(it)
				}
				Snackbar.make(gui.coordinatorLayout, R.string.csv_imported_successfully, Snackbar.LENGTH_LONG).show()
			} else {
				Snackbar.make(gui.coordinatorLayout, R.string.wrong_plate_number, Snackbar.LENGTH_LONG).show()
			}
		} catch (e: Exception) {
			e.printStackTrace()
			Snackbar.make(gui.coordinatorLayout, R.string.wrong_csv_format, Snackbar.LENGTH_LONG).show()
		}
	}

	private fun exportCsvFile(mileages: List<Mileage>) {
		val csvContent = StringBuilder("id,plateNumber,date,kilometres,litres,mileage,notes\n")

		mileages.forEach {
			csvContent.append(
				"${it.id}," +
						"${it.vehiclePlateNumber}," +
						"${Utils.formatDate(it.date)}," +
						"${it.kilometres}," +
						"${it.litres}," +
						"${it.mileage}," +
						"${it.notes}\n"
			)
		}
		val exportedFile = File(
			requireContext().cacheDir,
			"${mileageListVM.plateNumber}_exported_mileages.csv"
		)
		val fileWriter = FileWriter(exportedFile)
		fileWriter.apply {
			write(csvContent.toString())
			flush()
			close()
		}

		val shareFileIntent = Utils.getShareFileIntent(
			requireContext(),
			exportedFile,
			R.string.exported_csv_subject,
			R.string.exported_csv_text,
			R.string.save_csv
		)

		startActivity(shareFileIntent)
		showToast(R.string.csv_exported_successfully)
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
			.setNegativeButton(R.string.cancel) { _, _ -> } //Nothing to do here
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
					.setAction(R.string.undo) { mileageListVM.restoreDeletedVehicle() }
					.show()

				findNavController().navigateUp()
			}
			.setNegativeButton(R.string.cancel) { _, _ -> } //Nothing to do here
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
				dateContentTextView.text = Utils.formatDate(mileage.date)
				kilometresContentTextView.text = String.format(Locale.getDefault(), "%.1f", mileage.kilometres)
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
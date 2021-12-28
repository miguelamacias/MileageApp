package com.macisdev.mileageapp

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.macisdev.mileageapp.databinding.FragmentMileageListBinding
import com.macisdev.mileageapp.model.Mileage
import java.util.*

private const val ARG_VEHICLE = "vehicle_arg"

class MileageListFragment : Fragment() {
	private lateinit var gui: FragmentMileageListBinding
	private lateinit var mileagesList: List<Mileage>

	private val fragmentArgs: MileageListFragmentArgs by navArgs()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentMileageListBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		mileagesList = MileageRepository.getMileages(fragmentArgs.vehiclePlateNumber)

		gui.mileagesRecyclerView.layoutManager = LinearLayoutManager(view.context)
		val adapter = MileageAdapter()
		gui.mileagesRecyclerView.adapter = adapter
		adapter.submitList(mileagesList.reversed())

		gui.recordsCountTextView.text = mileagesList.size.toString()
		gui.averageMileageTextView.text = calculateAverage()
	}

	private fun calculateAverage() = String.format(Locale.getDefault(), "%.2f",
		mileagesList.sumOf { it.mileage } / mileagesList.size)

	companion object {
		fun newInstance(vehiclePlate: String) =
			MileageListFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_VEHICLE, vehiclePlate)
				}
			}
	}

	private inner class MileageAdapter() :
		ListAdapter<Mileage, MileageAdapter.MileageViewHolder>(MileageDiffCallback) {

		private inner class MileageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
			private var currentMileage: Mileage? = null
			val mileageContentTextView: TextView = view.findViewById(R.id.mileage_content_text_view)
			val dateContentTextView: TextView = view.findViewById(R.id.date_content_text_view)
			val kilometresContentTextView: TextView = view.findViewById(R.id.kilometres_content_text_view)
			val litresContentTextView: TextView = view.findViewById(R.id.litres_content_text_view)
			val notesTextView: TextView = view.findViewById(R.id.notes_text_view)

			fun bindData(mileage: Mileage) {
				currentMileage = mileage

				mileageContentTextView.text = String.format(Locale.getDefault(), "%.2f", mileage.mileage)
				dateContentTextView.text = DateFormat.format("dd/MM/yy", mileage.date)
				kilometresContentTextView.text = mileage.kilometres.toString()
				litresContentTextView.text = mileage.litres.toString()
				notesTextView.text = mileage.notes
			}
		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MileageAdapter.MileageViewHolder {
			return MileageViewHolder(layoutInflater.inflate(R.layout.list_item_mileage, parent, false))
		}

		override fun onBindViewHolder(holder: MileageAdapter.MileageViewHolder, position: Int) {
			holder.bindData(currentList[position])
		}

		override fun getItemCount() = currentList.size
	}

	object MileageDiffCallback : DiffUtil.ItemCallback<Mileage>() {
		override fun areItemsTheSame(oldItem: Mileage, newItem: Mileage): Boolean {
			return oldItem.id == newItem.id
		}

		override fun areContentsTheSame(oldItem: Mileage, newItem: Mileage): Boolean {
			return oldItem == newItem
		}
	}
}
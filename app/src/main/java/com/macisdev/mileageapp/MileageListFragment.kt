package com.macisdev.mileageapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.macisdev.mileageapp.databinding.FragmentMileageListBinding
import com.macisdev.mileageapp.model.Mileage
import java.util.*

private const val ARG_VEHICLE = "vehicle_arg"

class MileageListFragment : Fragment() {
	private lateinit var gui: FragmentMileageListBinding

	private var param1: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		arguments?.let {
			param1 = it.getString(ARG_VEHICLE)
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		gui = FragmentMileageListBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.mileagesRecyclerView.layoutManager = LinearLayoutManager(view.context)
		val adapter = MileageAdapter(MileageRepository.getMileages("8054FDG"))
		gui.mileagesRecyclerView.adapter = adapter
	}

	companion object {
		fun newInstance(vehiclePlate: String) =
			MileageListFragment().apply {
				arguments = Bundle().apply {
					putString(ARG_VEHICLE, vehiclePlate)
				}
			}
	}

	private inner class MileageAdapter(private val mileages: List<Mileage>) :
		RecyclerView.Adapter<MileageAdapter.MileageViewHolder>() {

		private inner class MileageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
			private var currentMileage: Mileage? = null
			val mileageContentTextView: TextView = view.findViewById(R.id.mileage_content_text_view)
			val dateContentTextView: TextView = view.findViewById(R.id.date_content_text_view)
			val kilometresContentTextView: TextView = view.findViewById(R.id.kilometres_content_text_view)
			val litresContentTextView: TextView = view.findViewById(R.id.litres_content_text_view)
			val notesTextView: TextView = view.findViewById(R.id.notes_text_view)

			fun bindData(mileage: Mileage) {
				currentMileage = mileage

				mileageContentTextView.text = mileage.mileage.toString()
				dateContentTextView.text = "28/12/21"
				kilometresContentTextView.text = mileage.kilometres.toString()
				litresContentTextView.text = mileage.litres.toString()
				notesTextView.text = mileage.notes
			}

		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MileageAdapter.MileageViewHolder {
			return  MileageViewHolder(layoutInflater.inflate(R.layout.list_item_mileage, parent, false))
		}

		override fun onBindViewHolder(holder: MileageAdapter.MileageViewHolder, position: Int) {
			holder.bindData(mileages[position])
		}

		override fun getItemCount() = mileages.size

	}
}
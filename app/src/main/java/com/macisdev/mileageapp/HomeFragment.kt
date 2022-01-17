package com.macisdev.mileageapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.macisdev.mileageapp.databinding.FragmentHomeBinding
import com.macisdev.mileageapp.model.Statistics
import com.macisdev.mileageapp.model.Vehicle
import com.macisdev.mileageapp.viewModels.HomeFragmentViewModel
import java.util.*

class HomeFragment : Fragment() {
	private lateinit var gui: FragmentHomeBinding
	private val homeFragmentVM: HomeFragmentViewModel by viewModels()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentHomeBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.vehiclesRecyclerView.apply {
			layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
			adapter = VehicleAdapter(gui.root)
		}

		homeFragmentVM.vehiclesList.observe(viewLifecycleOwner) { updateVehicles(it) }
		homeFragmentVM.getStatistics().observe(viewLifecycleOwner) {updateStatistics(it) }
	}

	private fun updateStatistics(stats: Statistics) {
		gui.recordsTv.text = stats.totalRecords.toString()
		gui.avgTv.text = String.format(Locale.getDefault(), "%.2f", stats.averageMileage)
		gui.litresTv.text = String.format(Locale.getDefault(), "%.0f", stats.totalLitres)
		gui.kilometresTv.text = String.format(Locale.getDefault(), "%.0f", stats.totalKilometres)
	}

	private fun updateVehicles(vehicles: List<Vehicle>) {
		val adapter = gui.vehiclesRecyclerView.adapter as VehicleAdapter

		val addIconEntryName = resources.getResourceEntryName(R.drawable.ic_add_circle)
		val addIconColor = resources.getResourceEntryName(R.color.add_icon_color)

		val addVehicle = Vehicle(getString(R.string.add_vehicle), "", "", addIconEntryName, addIconColor)

		val mutableVehicles = vehicles.toMutableList()
		mutableVehicles.add(addVehicle)

		adapter.submitList(mutableVehicles)
	}

	private inner class VehicleAdapter(val rootView: View) :
		ListAdapter<Vehicle, VehicleAdapter.VehicleViewHolder>(VehicleDiffCallback) {

		private inner class VehicleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
			private lateinit var currentVehicle: Vehicle
			private val vehicleIconImgView: ImageView = view.findViewById(R.id.vehicle_icon_img_view)
			private val vehicleTitleTv: TextView = view.findViewById(R.id.vehicle_title_tv)
			private val vehicleSubTitleTv: TextView = view.findViewById(R.id.vehicle_sub_tittle_tv)

			init {
				itemView.setOnClickListener {
					if (currentVehicle.plateNumber == getString(R.string.add_vehicle)) {
						val directions = HomeFragmentDirections
							.actionHomeFragmentToAddVehicleFragment(false, "")
						findNavController().navigate(directions)
					} else {
						val directions = HomeFragmentDirections
							.actionHomeFragmentToMileageListFragment(
								currentVehicle.plateNumber,
								currentVehicle.maker,
								currentVehicle.model
							)
						findNavController().navigate(directions)
					}
				}
			}

			@SuppressLint("SetTextI18n")
			fun bindData(vehicle: Vehicle) {
				currentVehicle = vehicle

				val iconId = resources.getIdentifier(vehicle.icon, "drawable", requireActivity().packageName)
				val colorId = resources.getIdentifier(vehicle.color, "color", requireActivity().packageName)
				val color = ContextCompat.getColor(rootView.context, colorId)

				vehicleIconImgView.setImageResource(iconId)
				vehicleIconImgView.setColorFilter(color)

				vehicleTitleTv.text = currentVehicle.plateNumber
				vehicleSubTitleTv.text = "${vehicle.maker} ${vehicle.model}"
			}
		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
			val layoutInflater = LayoutInflater.from(parent.context)
			return VehicleViewHolder(layoutInflater.inflate(R.layout.list_item_vehicle, parent, false))
		}

		override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
			holder.bindData(currentList[position])
		}
	}

	private object VehicleDiffCallback : DiffUtil.ItemCallback<Vehicle>() {
		override fun areItemsTheSame(oldItem: Vehicle, newItem: Vehicle): Boolean {
			return oldItem.plateNumber == newItem.plateNumber
		}

		override fun areContentsTheSame(oldItem: Vehicle, newItem: Vehicle): Boolean {
			return oldItem == newItem
		}
	}
}
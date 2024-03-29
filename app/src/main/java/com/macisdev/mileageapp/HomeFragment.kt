package com.macisdev.mileageapp

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.macisdev.mileageapp.api.fuel.FuelStation
import com.macisdev.mileageapp.databinding.FragmentHomeBinding
import com.macisdev.mileageapp.model.Mileage
import com.macisdev.mileageapp.model.Statistics
import com.macisdev.mileageapp.model.Vehicle
import com.macisdev.mileageapp.utils.Constants
import com.macisdev.mileageapp.utils.Constants.Companion.HIDE_ADD_VEHICLE_PREFERENCE
import com.macisdev.mileageapp.utils.Utils
import com.macisdev.mileageapp.viewModels.HomeFragmentViewModel
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var gui: FragmentHomeBinding
    private val homeFragmentVM: HomeFragmentViewModel by viewModels()
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        gui = FragmentHomeBinding.inflate(inflater, container, false)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        return gui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gui.vehiclesRecyclerView.apply {
            layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = VehicleAdapter(gui.root)
        }

        val fuelServiceActivated = preferences.getBoolean(Constants.FUEL_SERVICE_ACTIVATION_PREFERENCE, false)
        if (fuelServiceActivated) {
            val preferredStationId = preferences.getInt(Constants.PREFERRED_GAS_STATION_ID, 0)
            val preferredStationCity = preferences.getInt(Constants.PREFERRED_GAS_STATION_CITY_ID, 0)
            homeFragmentVM.getFuelStationById(preferredStationCity, preferredStationId).observe(viewLifecycleOwner) {
                updateFuelStationInfo(it)
            }
        } else {
            gui.favouriteFuelStationCardView.visibility = View.GONE
        }

        homeFragmentVM.vehiclesList.observe(viewLifecycleOwner) { updateVehicles(it) }
        homeFragmentVM.getStatistics().observe(viewLifecycleOwner) { updateStatistics(it) }
        homeFragmentVM.getLastMileage().observe(viewLifecycleOwner) { updateLastMileage(it) }
    }

    private fun updateFuelStationInfo(station: FuelStation) {
        if (station.iDEESS != 0) {
            station.replaceEmptyPrices()

            showPricesChangedIcons(station)

            gui.fuelStationNameTextView.text = station.rotulo.plus(" (").plus(station.municipio).plus(")")
            gui.timesFuelStationTextView.text = station.horario

            val currencySign = Currency.getInstance(Locale.getDefault()).symbol
            val dieselPrice = station.precioGasoleoA.replace(',', '.').toDouble()
            val petrolPrice = station.precioGasolina95E5.replace(',', '.').toDouble()

            gui.dieselPriceTextView.text = if (dieselPrice > 0) {
                String.format(Locale.getDefault(), "%.3f%s", dieselPrice, currencySign)
            } else {
                "-"
            }

            gui.petrolPriceTextView.text = if (petrolPrice > 0) {
                String.format(Locale.getDefault(), "%.3f%s", petrolPrice, currencySign)
            } else {
                "-"
            }

            gui.lastStationUpdateTextView.text = getString(R.string.update_station_info, Utils.formatDate(Date()))

            gui.favouriteFuelStationCardView.setOnClickListener {
                val directions = HomeFragmentDirections.actionHomeFragmentToFuelStationsFragment(station.CP)
                findNavController().navigate(directions)
            }
        } else {
            gui.favouriteFuelStationCardView.visibility = View.GONE
        }
    }

    private fun showPricesChangedIcons(currentStation: FuelStation) {
        val daysToSubtract = preferences.getString(Constants.COMPARISON_PERIOD_FUEL_PRICES, "0")?.toLong() ?: 0
        val localDate = LocalDate.now().minusDays(daysToSubtract)
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())

        homeFragmentVM.getHistoricalData(date, currentStation.iDMunicipio, currentStation.iDEESS)
            .observe(viewLifecycleOwner) { historicalStation ->
                historicalStation.replaceEmptyPrices()

                if (historicalStation.precioGasoleoA > currentStation.precioGasoleoA) {
                    gui.dieselPriceTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, R.drawable.ic_price_down_24, 0)
                } else if (historicalStation.precioGasoleoA < currentStation.precioGasoleoA) {
                    gui.dieselPriceTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, R.drawable.ic_price_up_24, 0)
                } else {
                    gui.dieselPriceTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0,0, R.drawable.ic_price_stay_24,0)
                }

                if (historicalStation.precioGasolina95E5 > currentStation.precioGasolina95E5) {
                    gui.petrolPriceTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, R.drawable.ic_price_down_24, 0)
                } else if (historicalStation.precioGasolina95E5 < currentStation.precioGasolina95E5) {
                    gui.petrolPriceTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, R.drawable.ic_price_up_24, 0)
                } else {
                    gui.petrolPriceTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        0, 0, R.drawable.ic_price_stay_24, 0)
                }
            }
    }

    private fun updateStatistics(stats: Statistics) {
        gui.recordsTv.text = stats.totalRecords.toString()
        gui.avgTv.text = String.format(
            Locale.getDefault(),
            "%.2f %s", stats.averageMileage, getString(R.string.mileage_unit)
        )
        gui.litresTv.text = String.format(
            Locale.getDefault(),
            "%,.0f %s", stats.totalLitres, getString(R.string.litres_l)
        )
        gui.kilometresTv.text = String.format(
            Locale.getDefault(),
            "%,.0f %s", stats.totalKilometres, getString(R.string.kilometres_km)
        )
    }

    private fun updateLastMileage(mileage: Mileage?) {
        if (mileage != null) {
            gui.lastMileageCardView.visibility = View.VISIBLE

            homeFragmentVM.getVehicle(mileage.vehiclePlateNumber).observe(viewLifecycleOwner) { vehicle ->
                if (vehicle != null) {
                    gui.vehicleInfoTextView.text = String.format(
                        Locale.getDefault(), "%s %s - %s",
                        vehicle.maker, vehicle.model, vehicle.plateNumber
                    )

                    gui.lastMileageCardView.setOnClickListener {
                        val directions = HomeFragmentDirections.actionHomeFragmentToMileageListFragment(
                            vehicle.plateNumber, vehicle.maker, vehicle.model
                        )
                        findNavController().navigate(directions)
                    }
                }
            }

            gui.dateTextView.text = Utils.formatDate(mileage.date)
            gui.litresTextView.text = String.format(Locale.getDefault(), "%.2f L", mileage.litres)
            gui.kilometresTextView.text = String.format(Locale.getDefault(), "%.1f KM", mileage.kilometres)
            gui.notesTextView.text = mileage.notes
            gui.mileageDataTextView.text = String.format(Locale.getDefault(), "%.2f", mileage.mileage)
        } else {
            gui.lastMileageCardView.visibility = View.INVISIBLE
        }
    }

    private fun updateVehicles(vehicles: List<Vehicle>) {
        val adapter = gui.vehiclesRecyclerView.adapter as VehicleAdapter

        val addIconEntryName = resources.getResourceEntryName(R.drawable.ic_add_circle)
        val addIconColor = resources.getResourceEntryName(R.color.add_icon_color)

        val mutableVehicles = vehicles.toMutableList()

        if (!preferences.getBoolean(HIDE_ADD_VEHICLE_PREFERENCE, false)
            || vehicles.isEmpty()
        ) {
            val addVehicle = Vehicle(getString(R.string.add_vehicle), "", "", addIconEntryName, addIconColor)
            mutableVehicles.add(addVehicle)
        }

        adapter.submitList(mutableVehicles)

        gui.numOfVehiclesTextView.text = vehicles.size.toString()
    }

    private inner class VehicleAdapter(val rootView: View) :
        ListAdapter<Vehicle, VehicleAdapter.VehicleViewHolder>(VehicleDiffCallback) {

        private inner class VehicleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private lateinit var currentVehicle: Vehicle
            private val vehicleIconImgView: ImageView = view.findViewById(R.id.vehicle_icon_img_view)
            private val vehicleTitleTv: TextView = view.findViewById(R.id.vehicle_title_tv)
            private val vehicleSubTitleTv: TextView = view.findViewById(R.id.vehicle_sub_tittle_tv)

            init {
                itemView.setOnLongClickListener {
                    if (currentVehicle.plateNumber != getString(R.string.add_vehicle)) {
                        var vehicleMileages: List<Mileage> = emptyList()

                        homeFragmentVM.getMileages(currentVehicle.plateNumber).observe(viewLifecycleOwner) {
                            vehicleMileages = it
                        }

                        val popup = PopupMenu(view.context, vehicleTitleTv)
                        popup.inflate(R.menu.menu_popup_vehicle)
                        popup.setOnMenuItemClickListener {
                            when (it.itemId) {
                                R.id.edit_vehicle -> {
                                    val directions = HomeFragmentDirections.actionHomeFragmentToAddVehicleFragment(
                                        true, currentVehicle.plateNumber
                                    )
                                    findNavController().navigate(directions)
                                    true
                                }

                                R.id.delete_vehicle -> {
                                    AlertDialog.Builder(requireContext())
                                        .setTitle(R.string.delete_vehicle)
                                        .setMessage(R.string.action_cannot_be_undone)
                                        .setPositiveButton(R.string.accept) { _, _ ->
                                            homeFragmentVM.deleteVehicle(currentVehicle, vehicleMileages)
                                            Snackbar
                                                .make(gui.root, R.string.deleted_vehicle, Snackbar.LENGTH_LONG)
                                                .setAction(R.string.undo) { homeFragmentVM.restoreDeletedVehicle() }
                                                .show()
                                        }
                                        .setNegativeButton(R.string.cancel, null)
                                        .show()

                                    true
                                }
                                else -> false
                            }
                        }
                        popup.show()
                        true
                    } else {
                        false
                    }
                }

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

            @SuppressLint("SetTextI18n", "DiscouragedApi")
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
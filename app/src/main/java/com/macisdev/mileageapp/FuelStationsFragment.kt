package com.macisdev.mileageapp

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.macisdev.mileageapp.api.fuel.ListaEESSPrecio
import com.macisdev.mileageapp.databinding.FragmentFuelStationsBinding
import com.macisdev.mileageapp.utils.Constants
import com.macisdev.mileageapp.utils.hideKeyboard
import com.macisdev.mileageapp.utils.showToast
import com.macisdev.mileageapp.viewModels.FuelStationsFragmentViewModel
import java.util.*


class FuelStationsFragment : Fragment() {
    private val fuelStationsViewModel: FuelStationsFragmentViewModel by viewModels()
    private lateinit var gui: FragmentFuelStationsBinding
    private lateinit var fuelStationAdapter: FuelStationAdapter
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        gui = FragmentFuelStationsBinding.inflate(inflater, container, false)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        return gui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gui.searchButton.setOnClickListener {
            searchFuelStations(gui.zipEditText.text.toString())
        }

        gui.zipEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchFuelStations(gui.zipEditText.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }

        gui.fuelStationsList.layoutManager = LinearLayoutManager(view.context)
        fuelStationAdapter = FuelStationAdapter()
        gui.fuelStationsList.adapter = fuelStationAdapter
    }

    private fun searchFuelStations(zip: String) {
        if (zip.isDigitsOnly() && zip.length == 5) {
            hideKeyboard()
            fuelStationAdapter.submitList(emptyList())
            gui.cityNameEditText.setText("")
            gui.fuelStationProgressBar.visibility = View.VISIBLE

            val zipNoLeadingZeros = zip.toInt().toString()

            fuelStationsViewModel.getCityCodeByZipCode(zipNoLeadingZeros).observe(viewLifecycleOwner) { cityCode ->

                if (cityCode != 0) {
                    fuelStationsViewModel.getFuelStationsByCityCode(cityCode)
                        .observe(viewLifecycleOwner) { fuelStationsList ->
                            if (fuelStationsList.isNotEmpty()) {
                                gui.cityNameEditText.setText(fuelStationsList?.first()?.municipio ?: "")

                                processFuelPrices(fuelStationsList)
                                fuelStationAdapter.submitList(fuelStationsList)
                            } else {
                                showToast(R.string.no_results_zip)
                            }
                        }
                } else {
                    showToast(getString(R.string.no_results_zip, zip))
                }

                gui.fuelStationProgressBar.visibility = View.GONE
            }
        } else {
            showToast(R.string.wrong_zip)
        }
    }

    private fun processFuelPrices(stationsList: List<ListaEESSPrecio>) {
        stationsList.forEach { station ->
            if (station.precioGasoleoA.isBlank()) {
                station.precioGasoleoA = station.precioGasoleoPremium.ifBlank {
                    "0.0"
                }
            }

            if (station.precioGasolina95E5.isBlank()) {
                station.precioGasolina95E5 = if (station.precioGasolina95E5Premium.isNotBlank()) {
                    station.precioGasolina95E5Premium
                } else if (station.precioGasolina95E10.isNotBlank()) {
                    station.precioGasolina95E10
                } else if (station.precioGasolina98E5.isNotBlank()) {
                    station.precioGasolina98E5
                } else  if (station.precioGasolina98E10.isNotBlank()) {
                    station.precioGasolina98E10
                } else {
                    "0.0"
                }
            }
        }

        val stationCheapestDiesel = stationsList.filter { it.precioGasoleoA != "0.0" }
            .minByOrNull { it.precioGasoleoA }
        val stationCheapestPetrol = stationsList.filter { it.precioGasolina95E5 != "0.0" }
            .minByOrNull { it.precioGasolina95E5 }

        val stationMostExpensiveDiesel = stationsList.filter { it.precioGasoleoA != "0.0" }
            .maxByOrNull { it.precioGasoleoA }
        val stationMostExpensivePetrol = stationsList.filter { it.precioGasolina95E5 != "0.0"}
            .maxByOrNull { it.precioGasolina95E5 }

        stationsList.takeWhile { stationsList.size >= 2 }.forEach { station ->
            if (station.iDEESS == stationCheapestDiesel?.iDEESS) {
                station.cheapestDiesel = true
            }
            if (station.iDEESS == stationCheapestPetrol?.iDEESS) {
                station.cheapestPetrol = true
            }

            if (station.iDEESS == stationMostExpensiveDiesel?.iDEESS) {
                station.mostExpensiveDiesel = true
            }
            if (station.iDEESS == stationMostExpensivePetrol?.iDEESS) {
                station.mostExpensivePetrol = true
            }
        }
    }

    private inner class FuelStationAdapter :
        ListAdapter<ListaEESSPrecio, FuelStationAdapter.FuelStationViewHolder>(FuelStationDiffCallBack) {

        private inner class FuelStationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private var currentFuelStation: ListaEESSPrecio? = null
            private val fuelStationNameTv: TextView = view.findViewById(R.id.fuel_station_name_tv)
            private val fuelStationOpeningHoursTv: TextView = view.findViewById(R.id.opening_hours_tv)
            private val fuelStationAddressTv: TextView = view.findViewById(R.id.fuel_station_address_tv)
            private val dieselPriceTv: TextView = view.findViewById(R.id.diesel_price_tv)
            private val petrolPriceTv: TextView = view.findViewById(R.id.petrol_price_tv)

            init {
                itemView.setOnClickListener {
                    val fuelStationFullName = currentFuelStation?.rotulo + " " + currentFuelStation?.municipio
                    val stationId = currentFuelStation?.iDEESS
                    val cityId = currentFuelStation?.iDMunicipio
                    preferences.edit {
                        putInt(Constants.PREFERRED_GAS_STATION_ID, stationId ?: 0)
                        putString(Constants.PREFERRED_GAS_STATION_NAME, fuelStationFullName)
                        putInt(Constants.PREFERRED_GAS_STATION_CITY_ID, cityId ?: 0)
                    }

                    findNavController().navigateUp()
                }
            }

            fun bindData(fuelStation: ListaEESSPrecio) {
                currentFuelStation = fuelStation

                fuelStationNameTv.text = fuelStation.rotulo
                fuelStationOpeningHoursTv.text = fuelStation.horario
                fuelStationAddressTv.text = fuelStation.direccion

                var dieselPrice = fuelStation.precioGasoleoA.replace(',', '.').toDouble()
                var petrolPrice = fuelStation.precioGasolina95E5.replace(',', '.').toDouble()

                val applyDiscount = preferences.getBoolean(Constants.FUEL_SERVICE_DISCOUNT_PREFERENCE, false)
                if (applyDiscount) {
                    dieselPrice -= 0.2
                    petrolPrice -= 0.2
                }

                dieselPriceTv.text = if (dieselPrice > 0) {
                    String.format(Locale.getDefault(), "%.3f", dieselPrice)
                } else {
                    "-"
                }

                petrolPriceTv.text = if (petrolPrice > 0) {
                    String.format(Locale.getDefault(), "%.3f", petrolPrice)
                } else {
                    "-"
                }

                val resetColor = ContextCompat.getColor(requireContext(), R.color.quantum_black_secondary_text)
                dieselPriceTv.setTextColor(resetColor)
                petrolPriceTv.setTextColor(resetColor)

                val redColor = ContextCompat.getColor(requireContext(), R.color.red)
                if (fuelStation.mostExpensiveDiesel) {
                    dieselPriceTv.setTextColor(redColor)
                }
                if (fuelStation.mostExpensivePetrol) {
                    petrolPriceTv.setTextColor(redColor)
                }

                val greenColor = ContextCompat.getColor(requireContext(), R.color.light_green)
                if (fuelStation.cheapestDiesel) {
                    dieselPriceTv.setTextColor(greenColor)
                }
                if (fuelStation.cheapestPetrol) {
                    petrolPriceTv.setTextColor(greenColor)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FuelStationViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            return FuelStationViewHolder(
                layoutInflater.inflate(R.layout.list_item_fuel_station, parent, false)
            )
        }

        override fun onBindViewHolder(holder: FuelStationViewHolder, position: Int) {
            holder.bindData(currentList[position])
        }
    }

    private object FuelStationDiffCallBack : DiffUtil.ItemCallback<ListaEESSPrecio>() {
        override fun areItemsTheSame(oldItem: ListaEESSPrecio, newItem: ListaEESSPrecio): Boolean {
            return oldItem.iDEESS == newItem.iDEESS
        }

        override fun areContentsTheSame(oldItem: ListaEESSPrecio, newItem: ListaEESSPrecio): Boolean {
            return oldItem.iDEESS == newItem.iDEESS
        }

    }
}
package com.macisdev.mileageapp

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.macisdev.mileageapp.api.TRIP_DISTANCE_ZERO_RESULTS_ERROR
import com.macisdev.mileageapp.databinding.FragmentTripCostBinding
import com.macisdev.mileageapp.model.Vehicle
import com.macisdev.mileageapp.utils.Constants
import com.macisdev.mileageapp.utils.Constants.Companion.FUEL_PRICE_DATE
import com.macisdev.mileageapp.utils.Constants.Companion.FUEL_PRICE_KEY
import com.macisdev.mileageapp.utils.Constants.Companion.TRIP_HOME_CODE
import com.macisdev.mileageapp.utils.Constants.Companion.TRIP_HOME_FULL
import com.macisdev.mileageapp.utils.Constants.Companion.TRIP_HOME_SHORT
import com.macisdev.mileageapp.utils.Utils
import com.macisdev.mileageapp.utils.getDouble
import com.macisdev.mileageapp.utils.hideKeyboard
import com.macisdev.mileageapp.utils.putDouble
import com.macisdev.mileageapp.utils.showToast
import com.macisdev.mileageapp.viewModels.TripCostViewModel
import java.text.DecimalFormat
import java.text.ParseException
import java.util.Currency
import java.util.Date
import java.util.Locale

class TripCostFragment : Fragment() {
	private lateinit var gui: FragmentTripCostBinding
	private val tripCostViewModel: TripCostViewModel by viewModels()
	private lateinit var preferences: SharedPreferences

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentTripCostBinding.inflate(inflater, container, false)
		preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		loadViewsPreviousState()
		configureAddressAutocomplete()
		loadFuelPrice()

		var currentVehicleMileage = ""

		gui.mileageTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			when (checkedId) {
				R.id.vehicle_mileage_radio_button -> {
					gui.customMileageEditText.apply {
						isEnabled = false
						setText(currentVehicleMileage)
					}
					gui.vehicleSpinner.visibility = View.VISIBLE

				}

				R.id.custom_mileage_radio_button -> {
					gui.customMileageEditText.apply {
						currentVehicleMileage = text.toString()
						isEnabled = true
						setText("")
					}
					gui.vehicleSpinner.visibility = View.INVISIBLE
				}
			}
		}

		gui.checkShareTripCost.setOnCheckedChangeListener { _, isChecked ->
			when (isChecked) {
				true -> {
					gui.peopleTextView.visibility = View.VISIBLE
					gui.spinnerShareCost.visibility = View.VISIBLE
				}

				false -> {
					gui.peopleTextView.visibility = View.INVISIBLE
					gui.spinnerShareCost.visibility = View.INVISIBLE
				}
			}
		}

		tripCostViewModel.getVehicles().observe(viewLifecycleOwner) {
			gui.vehicleSpinner.adapter = ArrayAdapter(
				requireContext(),
				R.layout.support_simple_spinner_dropdown_item,
				it
			)
		}

		gui.vehicleSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
			override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
				val vehicle = parent?.selectedItem as Vehicle

				tripCostViewModel.getVehicleAverageMileage(vehicle.plateNumber).observe(viewLifecycleOwner) {
					gui.customMileageEditText.setText(String.format(Locale.getDefault(), "%.2f", it))
				}
			}

			override fun onNothingSelected(parent: AdapterView<*>?) {}

		}

		gui.calculateCostButton.setOnClickListener {
			calculateTripCost()
		}

		updateHomeIcon()
		gui.tripHomeButton.setOnClickListener {
			val currentHome = preferences.getString(TRIP_HOME_CODE, "") ?: ""
			val currentAddress = tripCostViewModel.originCode
			val samePlace = currentHome == tripCostViewModel.originCode

			if (currentHome.isNotBlank() && currentAddress.isBlank()) {
				loadCurrentHome()
			} else if (currentHome.isNotBlank() && !samePlace){
				setCurrentHome(true)
			} else if (currentHome.isBlank() && currentAddress.isNotBlank()) {
				setCurrentHome(false)
			} else if (currentHome.isBlank() && currentAddress.isBlank()){
				showNoHomeDialog()
			}
			updateHomeIcon()
		}
	}

	private fun showNoHomeDialog() {
		AlertDialog.Builder(requireContext())
			.setTitle(R.string.no_home_title)
			.setMessage(R.string.no_home_info)
			.setPositiveButton(R.string.accept, null)
			.show()
	}

	private fun updateHomeIcon() {
		val currentHome = preferences.getString(TRIP_HOME_CODE, "") ?: ""
		val currentPlace = tripCostViewModel.originCode

		if (currentPlace.isNotBlank() && (currentHome.isBlank() || currentHome != currentPlace)) {
			setHomeIconStatus(true)
		} else {
			setHomeIconStatus(false)
		}
	}

	private fun setHomeIconStatus(addIcon: Boolean) {
		if (addIcon) {
			gui.tripHomeButton.setImageResource(R.drawable.ic_add_home_24)
		} else {
			gui.tripHomeButton.setImageResource(R.drawable.ic_home_24)
		}
	}

	private fun setCurrentHome(overriding: Boolean) {
		if (overriding) {
			AlertDialog.Builder(requireContext())
				.setTitle(R.string.overriding_home)
				.setMessage(R.string.info_override_home)
				.setPositiveButton(R.string.accept) { _, _ ->
					preferences.edit {
						putString(TRIP_HOME_CODE, tripCostViewModel.originCode)
						putString(TRIP_HOME_FULL, tripCostViewModel.originFullAddress)
						putString(TRIP_HOME_SHORT, tripCostViewModel.originName)
					}
					updateHomeIcon()
					showToast(R.string.home_updated)
				}
				.setNegativeButton(R.string.cancel, null)
				.show()
		} else {
			preferences.edit {
				putString(TRIP_HOME_CODE, tripCostViewModel.originCode)
				putString(TRIP_HOME_FULL, tripCostViewModel.originFullAddress)
				putString(TRIP_HOME_SHORT, tripCostViewModel.originName)
			}
			updateHomeIcon()
			showToast(R.string.home_updated)
		}
	}

	private fun loadCurrentHome() {
		val autocompleteFragmentOrigin = childFragmentManager
			.findFragmentById(R.id.autocomplete_fragment_origin) as AutocompleteSupportFragment

		tripCostViewModel.originCode = preferences.getString(TRIP_HOME_CODE, "") ?: ""
		tripCostViewModel.originFullAddress = preferences.getString(TRIP_HOME_FULL, "") ?: ""
		tripCostViewModel.originName = preferences.getString(TRIP_HOME_SHORT, "") ?: ""

		autocompleteFragmentOrigin.setText(tripCostViewModel.originName)
		gui.originFullAddressTextView.text = tripCostViewModel.originFullAddress
	}

	private fun loadFuelPrice() {
		gui.fuelPriceEditText.setText(
			String.format(Locale.getDefault(), "%.3f", preferences.getDouble(FUEL_PRICE_KEY, 0.0))
		)
		val lastFuelPriceDate = preferences.getString(FUEL_PRICE_DATE, "") ?: ""
		if (lastFuelPriceDate.isNotBlank()) {
			gui.fuelOriginInfoTv.text =
				getString(R.string.fuel_last_stored_info, lastFuelPriceDate)
		}

		val firstTimeUsingCalculator = preferences.getBoolean(Constants.TRIP_CALCULATOR_FIRST_TIME, true)
		if (firstTimeUsingCalculator) {
			preferences.edit {
				putBoolean(Constants.TRIP_CALCULATOR_FIRST_TIME, false)
			}
			showFirstTimeDialog()
		}

		val fuelPriceServiceActivated = preferences.getBoolean(Constants.FUEL_SERVICE_ACTIVATION_PREFERENCE, false)
		if (fuelPriceServiceActivated) {
			loadAutomaticFuelPrice()
		}

		val currentFuelPrice = gui.fuelPriceEditText.text.toString()
		if (currentFuelPrice == "0.000" || currentFuelPrice == "0,000") {
			gui.fuelPriceEditText.setText("")
		}
	}

	private fun loadViewsPreviousState() {
		val autocompleteFragmentOrigin = childFragmentManager
			.findFragmentById(R.id.autocomplete_fragment_origin) as AutocompleteSupportFragment
		val autocompleteFragmentDestination = childFragmentManager
			.findFragmentById(R.id.autocomplete_fragment_destination) as AutocompleteSupportFragment

		autocompleteFragmentOrigin.setText(tripCostViewModel.originName)
		autocompleteFragmentDestination.setText(tripCostViewModel.destinationName)
		gui.originFullAddressTextView.text = tripCostViewModel.originFullAddress
		gui.destinationFullAddressTextView.text = tripCostViewModel.destinationFullAddress
		gui.resultsCardView.visibility = tripCostViewModel.resultsCardViewVisibility
		gui.tripDistanceTextView.text = tripCostViewModel.currentTripDistance
		gui.tripFuelTextView.text = tripCostViewModel.currentTripFuel
		gui.tripDurationTextView.text = tripCostViewModel.currentTripDuration
		gui.tripCostTextView.text = tripCostViewModel.currentTripCost
		gui.sharedTripCostTextView.text = tripCostViewModel.sharedTripCost
		gui.sharedTripCostTextView.visibility = tripCostViewModel.sharedTripCostVisibility
		gui.costByPassengerTextView.visibility = tripCostViewModel.sharedTripCostVisibility
		gui.googleTextView.visibility = tripCostViewModel.resultsCardViewVisibility
		updateHomeIcon()
	}

	private fun loadAutomaticFuelPrice() {
		val stationCityCode = preferences.getInt(Constants.PREFERRED_GAS_STATION_CITY_ID, 0)
		val stationId = preferences.getInt(Constants.PREFERRED_GAS_STATION_ID, 0)
		val fuelType = preferences.getString(Constants.FUEL_TYPE_PREFERENCE, "")
		var fuelPrice: Double

		tripCostViewModel.getFuelStationById(stationCityCode, stationId)
			.observe(viewLifecycleOwner) { station ->
				station.replaceEmptyPrices()

				fuelPrice = if (fuelType == getString(R.string.diesel)) {
					station.precioGasoleoA.replace(',', '.').toDouble()
				} else {
					station.precioGasolina95E5.replace(',', '.').toDouble()
				}

				if (fuelPrice > 0) {
					val stationInfo = station.rotulo + " " + station.municipio
					val date = station.creationDate
					val formattedDate = date
						.removeRange(date.lastIndexOf('/').plus(1), date.lastIndexOf('/').plus(3))
						.dropLast(3)
					gui.fuelOriginInfoTv.text = getString(R.string.fuel_origin_info,
						stationInfo, formattedDate)
					gui.fuelPriceEditText.setText(String.format(Locale.getDefault(), "%.3f", fuelPrice))
				}
		}
	}

	private fun configureAddressAutocomplete() {
		// Initialize the Places SDK
		Places.initialize(requireActivity().applicationContext, BuildConfig.MAPS_API_KEY)

		// Initialize the AutocompleteSupportFragments
		val autocompleteFragmentOrigin = childFragmentManager
			.findFragmentById(R.id.autocomplete_fragment_origin) as AutocompleteSupportFragment
		val autocompleteFragmentDestination = childFragmentManager
			.findFragmentById(R.id.autocomplete_fragment_destination) as AutocompleteSupportFragment

		// Specify the types of place data to return
		autocompleteFragmentOrigin.setPlaceFields(
			listOf(
				Place.Field.ID,
				Place.Field.ADDRESS,
				Place.Field.NAME
			)
		)

		autocompleteFragmentDestination.setPlaceFields(
			listOf(
				Place.Field.ID,
				Place.Field.ADDRESS,
				Place.Field.NAME
			)
		)

		autocompleteFragmentOrigin.setActivityMode(AutocompleteActivityMode.FULLSCREEN)
		autocompleteFragmentDestination.setActivityMode(AutocompleteActivityMode.FULLSCREEN)

		autocompleteFragmentOrigin.setHint(getString(R.string.search_origin))
		autocompleteFragmentDestination.setHint(getString(R.string.search_destination))

		// Set up a PlaceSelectionListener to handle the response.
		autocompleteFragmentOrigin.setOnPlaceSelectedListener(object : PlaceSelectionListener {
			override fun onPlaceSelected(place: Place) {
				gui.originFullAddressTextView.text = place.address
				tripCostViewModel.originCode = "place_id:".plus(place.id)
				tripCostViewModel.originName = place.name ?: ""
				tripCostViewModel.originFullAddress = place.address ?: ""

				autocompleteFragmentOrigin.apply {
					setText(place.name)

					view?.findViewById<ImageView>(R.id.places_autocomplete_clear_button)
						?.setOnClickListener {
							setText("")
							gui.originFullAddressTextView.text = ""
							tripCostViewModel.originFullAddress = ""
							tripCostViewModel.originCode = ""
						}
				}
				updateHomeIcon()
			}

			override fun onError(status: Status) {
				Log.i(MainActivity.TAG, "An error occurred: $status")
			}
		})

		autocompleteFragmentDestination.setOnPlaceSelectedListener(object : PlaceSelectionListener {
			override fun onPlaceSelected(place: Place) {
				gui.destinationFullAddressTextView.text = place.address
				tripCostViewModel.destinationCode = "place_id:".plus(place.id)
				tripCostViewModel.destinationName = place.name ?: ""
				tripCostViewModel.destinationFullAddress = place.address ?: ""

				autocompleteFragmentDestination.apply {
					setText(place.name)

					view?.findViewById<ImageView>(R.id.places_autocomplete_clear_button)
						?.setOnClickListener {
							setText("")
							gui.destinationFullAddressTextView.text = ""
							tripCostViewModel.destinationFullAddress = ""
							tripCostViewModel.destinationCode = ""
						}
				}
				updateHomeIcon()
			}

			override fun onError(status: Status) {
				Log.i(MainActivity.TAG, "An error occurred: $status")
			}
		})

	}

	private fun calculateTripCost() {
		hideKeyboard()
		gui.loadingBar.visibility = View.VISIBLE
		gui.resultsCardView.visibility = View.GONE
		gui.googleTextView.visibility = View.GONE
		gui.rootScrollView.post {
			gui.rootScrollView.smoothScrollTo(0, gui.loadingBar.bottom)
		}

		var validData = true

		val decimalFormatter = DecimalFormat.getInstance(Locale.getDefault())

		val mileage = try {
			decimalFormatter.parse(gui.customMileageEditText.text.toString())?.toDouble() ?: 0.0
		} catch (e: ParseException) {
			0.0
		}

		val fuelPrice = try {
			decimalFormatter.parse(gui.fuelPriceEditText.text.toString())?.toDouble() ?: 0.0
		} catch (e: ParseException) {
			0.0
		}

		val avoidTolls = gui.avoidTollsCheckBox.isChecked

		if (tripCostViewModel.originCode == "" && tripCostViewModel.destinationCode == "") {
			showToast(R.string.origin_destination_missing)
			validData = false
		} else if (tripCostViewModel.originCode == "") {
			showToast(R.string.origin_missing)
			validData = false
		} else if (tripCostViewModel.destinationCode == "") {
			showToast(R.string.destination_missing)
			validData = false
		}

		if (mileage == 0.0) {
			gui.customMileageEditText.error = getString(R.string.enter_valid_mileage)
			validData = false
		}

		if (fuelPrice == 0.0) {
			gui.fuelPriceEditText.error = getString(R.string.enter_valid_fuel_price)
			validData = false
		}

		if (validData) {
			showTripCost(mileage, fuelPrice, avoidTolls)
		} else {
			gui.resultsCardView.visibility = View.GONE
			gui.loadingBar.visibility = View.GONE
		}
	}

	private fun showTripCost(mileage: Double, fuelPrice: Double, avoidTolls: Boolean) {
		preferences.edit {
			putDouble(FUEL_PRICE_KEY, fuelPrice)
			putString(FUEL_PRICE_DATE, Utils.formatDate(Date()))
		}

		tripCostViewModel.getTripDistance(avoidTolls).observe(viewLifecycleOwner) { oneWayDistance ->
			when {
				oneWayDistance > 0 -> {
					val tripDistance =
						if (gui.roundTripCheckBox.isChecked) oneWayDistance * 2 else oneWayDistance

					gui.tripDistanceTextView.text = String.format(
						Locale.getDefault(),
						"%,.2f KM", tripDistance
					)

					val tripLitres = tripDistance * mileage / 100.0
					gui.tripFuelTextView.text = String.format(
						Locale.getDefault(), "%,.2f L", tripLitres
					)

					val currencySign = Currency.getInstance(Locale.getDefault()).symbol
					val tripCost = tripLitres * fuelPrice
					gui.tripCostTextView.text = String.format(
						Locale.getDefault(),
						"%.2f%s", tripCost, currencySign
					)

					if (gui.checkShareTripCost.isChecked) {
						gui.costByPassengerTextView.visibility = View.VISIBLE
						gui.sharedTripCostTextView.visibility = View.VISIBLE
						val passengers = gui.spinnerShareCost.selectedItem.toString().toDouble()
						gui.sharedTripCostTextView.text = String.format(
							Locale.getDefault(),
							"%.2f%s", tripCost / passengers, currencySign)
					} else {
						gui.costByPassengerTextView.visibility = View.GONE
						gui.sharedTripCostTextView.visibility = View.GONE
					}

					tripCostViewModel.getTripDuration().observe(viewLifecycleOwner) { oneWayDuration ->
						val tripDuration =
							if (gui.roundTripCheckBox.isChecked) oneWayDuration * 2 else oneWayDuration
						val hours = tripDuration / 60
						val minutes = tripDuration % 60
						gui.tripDurationTextView.text = getString(R.string.hours_minutes, hours, minutes)
					}

					gui.resultsCardView.visibility = View.VISIBLE
					gui.loadingBar.visibility = View.GONE
					gui.googleTextView.visibility = View.VISIBLE

					gui.rootScrollView.post {
						gui.rootScrollView.smoothScrollTo(0, gui.resultsCardView.bottom)
					}
				}

				oneWayDistance == TRIP_DISTANCE_ZERO_RESULTS_ERROR -> {
					showToast(R.string.no_routes_found)
					gui.resultsCardView.visibility = View.GONE
					gui.loadingBar.visibility = View.GONE
					gui.googleTextView.visibility = View.VISIBLE
				}

				else -> {
					showToast(R.string.something_wrong)
					gui.resultsCardView.visibility = View.GONE
					gui.loadingBar.visibility = View.GONE
					gui.googleTextView.visibility = View.VISIBLE
				}
			}
		}
	}

	private fun showFirstTimeDialog() {
		AlertDialog.Builder(requireContext())
			.setTitle(R.string.welcome_to_trip_calculator)
			.setMessage(R.string.info_auto_fuel_price)
			.setPositiveButton(R.string.try_it_out) { _, _ ->
				preferences.edit {
					putBoolean(Constants.FUEL_SERVICE_ACTIVATION_PREFERENCE, true)
				}
				val directions = TripCostFragmentDirections.actionTripCostFragmentToSettingsFragment()
				findNavController().navigate(directions)
			}
			.setNegativeButton(R.string.maybe_later) { _, _ ->
				showExtraDialog()
			}
			.show()
	}

	private fun showExtraDialog() {
		AlertDialog.Builder(requireContext())
			.setMessage(R.string.change_mind)
			.setPositiveButton(android.R.string.ok, null)
			.show()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		tripCostViewModel.currentTripDistance = gui.tripDistanceTextView.text.toString()
		tripCostViewModel.currentTripFuel = gui.tripFuelTextView.text.toString()
		tripCostViewModel.currentTripDuration = gui.tripDurationTextView.text.toString()
		tripCostViewModel.currentTripCost = gui.tripCostTextView.text.toString()
		tripCostViewModel.sharedTripCost = gui.sharedTripCostTextView.text.toString()
		tripCostViewModel.sharedTripCostVisibility = gui.sharedTripCostTextView.visibility
		tripCostViewModel.resultsCardViewVisibility = gui.resultsCardView.visibility
	}
}
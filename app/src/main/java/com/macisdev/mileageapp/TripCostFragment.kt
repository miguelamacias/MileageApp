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
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.macisdev.mileageapp.database.TRIP_DISTANCE_ZERO_RESULTS_ERROR
import com.macisdev.mileageapp.databinding.FragmentTripCostBinding
import com.macisdev.mileageapp.model.Vehicle
import com.macisdev.mileageapp.utils.getDouble
import com.macisdev.mileageapp.utils.hideKeyboard
import com.macisdev.mileageapp.utils.putDouble
import com.macisdev.mileageapp.utils.showToast
import com.macisdev.mileageapp.viewModels.TripCostViewModel
import java.text.DecimalFormat
import java.text.ParseException
import java.util.*

class TripCostFragment : Fragment() {
	companion object {
		private const val FUEL_PRICE_KEY = "fuelPriceKey"
	}

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
		configureAdressAutocomplete()

		gui.originFullAdressTextView.text = tripCostViewModel.originAddress
		gui.destinationFullAdressTextView.text = tripCostViewModel.destinationAddress
		gui.resultsCardView.visibility = tripCostViewModel.resultsCardViewVisibility
		gui.tripDistanceTextView.text = tripCostViewModel.currentTripDistance
		gui.tripFuelTextView.text = tripCostViewModel.currentTripFuel
		gui.tripDurationTextView.text = tripCostViewModel.currentTripDuration
		gui.tripCostTextView.text = tripCostViewModel.currentTripCost
		gui.googleTextView.visibility = tripCostViewModel.resultsCardViewVisibility

		gui.fuelPriceEditText.setText(
			String.format(
				Locale.getDefault(), "%.3f",
				preferences.getDouble(FUEL_PRICE_KEY, 0.0)
			)
		)

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
	}

	private fun configureAdressAutocomplete() {
		// Initialize the Places SDK
		Places.initialize(requireActivity().applicationContext, BuildConfig.MAPS_API_KEY)

		// Initialize the AutocompleteSupportFragments
		val autocompleteFragmentOrigin = childFragmentManager
			.findFragmentById(R.id.autocomplete_fragment_origin) as AutocompleteSupportFragment
		val autocompleteFragmentDestination = childFragmentManager
			.findFragmentById(R.id.autocomplete_fragment_destiny) as AutocompleteSupportFragment

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
				gui.originFullAdressTextView.text = place.address

				tripCostViewModel.origin = "place_id:".plus(place.id)
				tripCostViewModel.originAddress = place.address ?: ""

				autocompleteFragmentOrigin.apply {
					setText(place.name)

					view?.findViewById<ImageView>(R.id.places_autocomplete_clear_button)
						?.setOnClickListener {
							setText("")
							gui.originFullAdressTextView.text = ""
							tripCostViewModel.originAddress = ""
							tripCostViewModel.origin = ""
						}
				}
			}

			override fun onError(status: Status) {
				Log.i(MainActivity.TAG, "An error occurred: $status")
			}
		})

		autocompleteFragmentDestination.setOnPlaceSelectedListener(object : PlaceSelectionListener {
			override fun onPlaceSelected(place: Place) {
				gui.destinationFullAdressTextView.text = place.address

				tripCostViewModel.destination = "place_id:".plus(place.id)
				tripCostViewModel.destinationAddress = place.address ?: ""

				autocompleteFragmentDestination.apply {
					setText(place.name)

					view?.findViewById<ImageView>(R.id.places_autocomplete_clear_button)
						?.setOnClickListener {
							setText("")
							gui.destinationFullAdressTextView.text = ""
							tripCostViewModel.destinationAddress = ""
							tripCostViewModel.destination = ""
						}
				}
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

		if (tripCostViewModel.origin == "" && tripCostViewModel.destination == "") {
			showToast(R.string.origin_destination_missing)
			validData = false
		} else if (tripCostViewModel.origin == "") {
			showToast(R.string.origin_missing)
			validData = false
		} else if (tripCostViewModel.destination == "") {
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
		preferences.edit { putDouble(FUEL_PRICE_KEY, fuelPrice) }

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

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		tripCostViewModel.currentTripDistance = gui.tripDistanceTextView.text.toString()
		tripCostViewModel.currentTripFuel = gui.tripFuelTextView.text.toString()
		tripCostViewModel.currentTripDuration = gui.tripDurationTextView.text.toString()
		tripCostViewModel.currentTripCost = gui.tripCostTextView.text.toString()
		tripCostViewModel.resultsCardViewVisibility = gui.resultsCardView.visibility
	}
}
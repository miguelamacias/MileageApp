package com.macisdev.mileageapp

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
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
import com.macisdev.mileageapp.databinding.FragmentTripCostBinding
import com.macisdev.mileageapp.model.Vehicle
import com.macisdev.mileageapp.utils.getDouble
import com.macisdev.mileageapp.utils.hideKeyboard
import com.macisdev.mileageapp.utils.putDouble
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

	private lateinit var origin: String
	private lateinit var destination: String
	private lateinit var preferences: SharedPreferences

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentTripCostBinding.inflate(inflater, container, false)
		preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		configureAdressAutocomplete()

		gui.fuelPriceEditText.setText(String.format(Locale.getDefault(), "%.3f",
			preferences.getDouble(FUEL_PRICE_KEY, 0.0)))

		gui.mileageTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
			when (checkedId) {
				R.id.vehicle_mileage_radio_button -> {
					gui.customMileageEditText.isEnabled = false
					gui.vehicleSpinner.visibility = View.VISIBLE
				}

				R.id.custom_mileage_radio_button -> {
					gui.customMileageEditText.apply {
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
			showTripCost()
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
				Place.Field.ADDRESS,
				Place.Field.NAME
			)
		)

		autocompleteFragmentDestination.setPlaceFields(
			listOf(
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
				autocompleteFragmentOrigin.setText(place.name)
				origin = place.address ?: ""
			}

			override fun onError(status: Status) {
				Log.i(MainActivity.TAG, "An error occurred: $status")
			}
		})

		autocompleteFragmentDestination.setOnPlaceSelectedListener(object : PlaceSelectionListener {
			@SuppressLint("SetTextI18n")
			override fun onPlaceSelected(place: Place) {
				gui.destinationFullAdressTextView.text = place.address
				autocompleteFragmentDestination.setText(place.name)
				destination = place.address ?: ""
			}

			override fun onError(status: Status) {
				Log.i(MainActivity.TAG, "An error occurred: $status")
			}
		})
	}

	private fun showTripCost() {
		try {
			gui.loadingBar.visibility = View.VISIBLE
			gui.resultsCardView.visibility = View.GONE
			hideKeyboard()

			val decimalFormatter = DecimalFormat.getInstance(Locale.getDefault())
			val mileage = decimalFormatter.parse(gui.customMileageEditText.text.toString())?.toDouble() ?: 0.0
			val fuelPrice = decimalFormatter.parse(gui.fuelPriceEditText.text.toString())?.toDouble() ?: 0.0

			val avoidTolls = gui.avoidTollsCheckBox.isChecked

			if(mileage > 0 && fuelPrice > 0) {
				preferences.edit {
					putDouble(FUEL_PRICE_KEY, fuelPrice)
				}

				tripCostViewModel
					.getTripDistance(origin, destination, avoidTolls).observe(viewLifecycleOwner) { oneWayDistance ->
						//cost = tripDistance * mileage / 100 * fuel price
						val tripDistance = if (gui.roundTripCheckBox.isChecked) oneWayDistance * 2 else oneWayDistance

						gui.tripDistanceTextView.text = String.format(Locale.getDefault(),
							"%,.2f KM", tripDistance)

						val tripLitres = tripDistance * mileage / 100.0
						gui.tripLitresTextView.text = String.format(Locale.getDefault(), "%,.2f L", tripLitres)

						val currencySign = Currency.getInstance(Locale.getDefault()).symbol
						val tripCost = tripLitres * fuelPrice
						gui.tripCostTextView.text = String.format(Locale.getDefault(),
							"%.2f%s", tripCost, currencySign)

						gui.resultsCardView.visibility = View.VISIBLE
						gui.loadingBar.visibility = View.GONE
					}
			} else {
				Toast.makeText(requireContext(), R.string.enter_valid_data, Toast.LENGTH_SHORT).show()
				gui.resultsCardView.visibility = View.GONE
				gui.loadingBar.visibility = View.GONE
			}
		} catch (e: ParseException) {
			Log.e(MainActivity.TAG, e.stackTraceToString())
			Toast.makeText(requireContext(), R.string.enter_valid_data, Toast.LENGTH_SHORT).show()
			gui.resultsCardView.visibility = View.GONE
			gui.loadingBar.visibility = View.GONE
		} catch (e: Exception) {
			Log.e(MainActivity.TAG, e.stackTraceToString())
			Toast.makeText(requireContext(), R.string.something_wrong, Toast.LENGTH_LONG).show()
			gui.resultsCardView.visibility = View.GONE
			gui.loadingBar.visibility = View.GONE
		}
	}
}
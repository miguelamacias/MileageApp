package com.macisdev.mileageapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.macisdev.mileageapp.databinding.FragmentTripCostBinding
import com.macisdev.mileageapp.model.Vehicle
import com.macisdev.mileageapp.viewModels.TripCostViewModel
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.Executors


class TripCostFragment : Fragment() {
	private lateinit var gui: FragmentTripCostBinding
	private val tripCostViewModel: TripCostViewModel by viewModels()

	private lateinit var origin: LatLng
	private lateinit var destination: LatLng

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentTripCostBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		configureAdressAutocomplete()

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
					gui.customMileageEditText.setText(it.toString())
				}
			}

			override fun onNothingSelected(parent: AdapterView<*>?) {}

		}

		gui.calculateCostButton.setOnClickListener {
			calculateCost()
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
				Place.Field.LAT_LNG,
				Place.Field.NAME
			)
		)

		autocompleteFragmentDestination.setPlaceFields(
			listOf(
				Place.Field.ADDRESS,
				Place.Field.LAT_LNG,
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
				origin = place.latLng ?: LatLng(0.0, 0.0)
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
				destination = place.latLng ?: LatLng(0.0, 0.0)
			}

			override fun onError(status: Status) {
				Log.i(MainActivity.TAG, "An error occurred: $status")
			}
		})
	}

	private fun calculateCost() {
		val client = OkHttpClient.Builder().build()

		val formattedOrigin = "${origin.latitude}%2C${origin.longitude}"
		val formattedDestination = "${destination.latitude}%2C${destination.longitude}"

		val url = "https://maps.googleapis.com/maps/api/distancematrix/json" +
					"?origins=$formattedOrigin&destinations=$formattedDestination&key=${BuildConfig.DISTANCE_MAPS_KEY}"

		val request = Request.Builder()
			.url(url)
			.method("GET", null)
			.build()

		val executor = Executors.newSingleThreadExecutor()


		executor.execute {
			val response = client.newCall(request).execute()
			val body = response.body!!.string()
			Log.d(MainActivity.TAG, body)

			val jsonObject = JSONObject(body)

			val tripDistance = jsonObject
				.getJSONArray("rows")
				.getJSONObject(0)
				.getJSONArray("elements")
				.getJSONObject(0)
				.getJSONObject("distance")
				.getDouble("value") / 1000

			//cost = tripDistance * mileage / 100 * fuel price
			val cost = tripDistance * gui.customMileageEditText.text.toString().toDouble() / 100 *
					gui.fuelPriceEditText.text.toString().toDouble()

			requireActivity().runOnUiThread {gui.resultCostTextView.text = cost.toString()}
		}
	}
}
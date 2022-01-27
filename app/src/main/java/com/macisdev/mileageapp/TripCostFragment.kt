package com.macisdev.mileageapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.macisdev.mileageapp.databinding.FragmentTripCostBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.Executors


class TripCostFragment : Fragment() {
	private lateinit var gui: FragmentTripCostBinding

	private lateinit var origin: String
	private lateinit var destination: String

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentTripCostBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		configureAdressAutocomplete()
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
			@SuppressLint("SetTextI18n")
			override fun onPlaceSelected(place: Place) {
				gui.originFullAdressTextView.text = place.address
				autocompleteFragmentOrigin.setText(place.name)
				origin = place.latLng?.toString() ?: ""
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
				destination = place.latLng?.toString() ?: ""
			}

			override fun onError(status: Status) {
				Log.i(MainActivity.TAG, "An error occurred: $status")
			}
		})
	}

	private fun calculateCost(origin: String, destination: String) {
		val client = OkHttpClient.Builder().build()

		val request = Request.Builder()
			.url("https://maps.googleapis.com/maps/api/distancematrix/json?origins=sevilla&destinations=madrid&key=AIzaSyByCDhk1XR6pqe0_6rT8CD0oJt-1IE4NsU")
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
			val cost = tripDistance * 7.5 / 100 * 1.299

			Log.d(MainActivity.TAG, cost.toString())
		}
	}
}
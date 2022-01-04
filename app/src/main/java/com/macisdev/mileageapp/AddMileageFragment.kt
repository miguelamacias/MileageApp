package com.macisdev.mileageapp


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.Toast

import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import com.macisdev.mileageapp.databinding.FragmentAddMileageBinding
import com.macisdev.mileageapp.model.Mileage
import java.text.DecimalFormat
import java.text.ParseException
import java.util.*


class AddMileageFragment : BottomSheetDialogFragment() {

	private lateinit var gui: FragmentAddMileageBinding
	private val fragmentArgs: AddMileageFragmentArgs by navArgs()
	private val mileageRepository = MileageRepository.get()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentAddMileageBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.kilometresEditText.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(sequence: Editable?) {
				if (gui.litresEditText.text.isNotEmpty()) calculateMileage()
			}

			override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {}
		})

		gui.litresEditText.addTextChangedListener(object : TextWatcher {
			override fun afterTextChanged(sequence: Editable?) {
				if (gui.kilometresEditText.text.isNotEmpty()) calculateMileage()
			}

			override fun beforeTextChanged(sequence: CharSequence?, start: Int, count: Int, after: Int) {}

			override fun onTextChanged(sequence: CharSequence?, start: Int, before: Int, count: Int) {}
		})

		gui.saveMileageButton.setOnClickListener {
			val decimalFormatter = DecimalFormat.getInstance(Locale.getDefault())

			val mileageData = decimalFormatter.parse(gui.mileageResultEditText.text.toString())?.toDouble() ?: -1.0

			if (mileageData > 0) {
				val date = Date()
				val kilometres = decimalFormatter.parse(gui.kilometresEditText.text.toString())?.toDouble() ?: -1.0
				val litres = decimalFormatter.parse(gui.litresEditText.text.toString())?.toDouble() ?: -1.0

				mileageRepository.storeMileage(Mileage(fragmentArgs.plateNumber, date, mileageData, kilometres, litres))

				parentFragment?.view?.let { parentView ->
					Snackbar.make(parentView, R.string.mileage_added, Snackbar.LENGTH_LONG).show()
				}

				findNavController().navigateUp()
			}
		}

		gui.dateEditText.apply {
			setText(DateFormat.format("dd/MM/yyyy", Date())) //TODO: User should be able to change the date
			setOnClickListener {
				Toast.makeText(context, R.string.not_available, Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun calculateMileage() {
		try {
			val decimalFormatter = DecimalFormat.getInstance(Locale.getDefault())

			val kilometres = decimalFormatter.parse(gui.kilometresEditText.text.toString())?.toDouble() ?: 0.0
			val litres = decimalFormatter.parse(gui.litresEditText.text.toString())?.toDouble() ?: 0.0
			val mileage = 100 * litres / kilometres

			if (mileage > 0) {
				gui.mileageResultEditText.setText(String.format(Locale.getDefault(), "%.2f", mileage))
				gui.saveMileageButton.isEnabled = true
			} else {
				gui.mileageResultEditText.setText(R.string.wrong_value)
				gui.saveMileageButton.isEnabled = false
			}

		} catch (e: ParseException) {
			gui.mileageResultEditText.setText(R.string.wrong_value)
			gui.saveMileageButton.isEnabled = false
		}
	}
}
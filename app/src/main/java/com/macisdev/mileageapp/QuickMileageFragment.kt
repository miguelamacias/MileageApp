package com.macisdev.mileageapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.macisdev.mileageapp.databinding.FragmentQuickMileageBinding
import java.text.DecimalFormat
import java.util.*

class QuickMileageFragment : Fragment() {
	private lateinit var gui: FragmentQuickMileageBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentQuickMileageBinding.inflate(inflater, container, false)

		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.calculateMileageButton.setOnClickListener {
			try {
				val decimalFormatter = DecimalFormat.getInstance(Locale.getDefault())

				val kilometres = decimalFormatter.parse(gui.kilometresEditText.text.toString())?.toDouble() ?: 0.0
				val litres = decimalFormatter.parse(gui.litresEditText.text.toString())?.toDouble() ?: 0.0
				val mileage = 100 * litres / kilometres

				if (mileage > 0) {
					gui.mileageResultEditText.setText(String.format(Locale.getDefault(),"%.2f", mileage))
				} else {
					gui.mileageResultEditText.setText(R.string.wrong_value)
				}

			} catch (e: Exception) {
				gui.mileageResultEditText.setText(R.string.wrong_value)
			}
		}
	}
}
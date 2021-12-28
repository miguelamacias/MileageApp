package com.macisdev.mileageapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.macisdev.mileageapp.databinding.FragmentQuickMileageBinding
import java.util.*

class QuickMileageFragment : Fragment() {
	private lateinit var gui: FragmentQuickMileageBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		gui = FragmentQuickMileageBinding.inflate(inflater, container, false)

		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.calculateMileageButton.setOnClickListener {
			try { //TODO: use a DecimalFormat to avoid bugs in differents locales
				val kilometres = gui.kilometresEditText.text.toString().toDouble()
				val litres = gui.litresEditText.text.toString().toDouble()
				val mileage = 100 * litres / kilometres

				if (mileage > 0) {
					gui.mileageResultEditText.setText(String.format(Locale.getDefault(),"%.2f", mileage))
				} else {
					gui.mileageResultEditText.setText(R.string.wrong_value)
				}

			} catch (e: NumberFormatException) {
				gui.mileageResultEditText.setText(R.string.wrong_value)
			}
		}
	}

	companion object {
		fun newInstance() = QuickMileageFragment()
	}
}
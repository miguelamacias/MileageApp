@file:Suppress("unused")

package com.macisdev.mileageapp.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.macisdev.mileageapp.MainActivity
import java.util.*

fun SharedPreferences.Editor.putDouble(key: String, double: Double): SharedPreferences.Editor =
	putLong(key, java.lang.Double.doubleToRawLongBits(double))

fun SharedPreferences.getDouble(key: String, default: Double) =
	java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))

fun Fragment.hideKeyboard() {
	view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
	hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
	val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
	inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.showToast(message: Int) {
	Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(message: String) {
	Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
}

fun Fragment.log(message: String) {
	Log.d(MainActivity.TAG, message)
}

fun Activity.log(message: String) {
	Log.d(MainActivity.TAG, message)
}

class Utils {
	companion object {
		fun formatDate(date: Date): String {
			return DateFormat.format(
				DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyy"),
				date).toString()
		}
	}
}
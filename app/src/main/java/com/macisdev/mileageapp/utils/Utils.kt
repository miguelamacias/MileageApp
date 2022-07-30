@file:Suppress("unused")

package com.macisdev.mileageapp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.macisdev.mileageapp.MainActivity
import com.macisdev.mileageapp.api.fuel.FuelStation
import java.io.File
import java.net.URLConnection
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

val Float.toPx: Int
	get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun AlertDialog.Builder.setContentPadded(view: View): AlertDialog.Builder {
	val container = FrameLayout(context)
	container.addView(view)
	val containerParams = FrameLayout.LayoutParams(
		FrameLayout.LayoutParams.WRAP_CONTENT,
		FrameLayout.LayoutParams.WRAP_CONTENT
	)
	val marginHorizontal = 48F
	val marginTop = 16F
	containerParams.topMargin = (marginTop / 2).toPx
	containerParams.leftMargin = marginHorizontal.toInt()
	containerParams.rightMargin = marginHorizontal.toInt()
	container.layoutParams = containerParams

	val superContainer = FrameLayout(context)
	superContainer.addView(container)

	setView(superContainer)

	return this
}

class Utils {
	companion object {
		fun formatDate(date: Date): String {
			return DateFormat.format(
				DateFormat.getBestDateTimePattern(Locale.getDefault(), "ddMMyy"),
				date).toString()
		}

		fun getShareFileIntent(
			context: Context,
			file: File,
			extraSubject: Int,
			extraText: Int,
			dialogText: Int): Intent? {

			if (file.exists()) {
				val fileUri = FileProvider.getUriForFile(
					context.applicationContext,
					"com.macisdev.mileageapp.fileprovider", file
				)

				val intentShareFile = Intent(Intent.ACTION_SEND).apply {
					setDataAndType(
						fileUri,
						URLConnection.guessContentTypeFromName(file.name)
					)

					addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
					putExtra(Intent.EXTRA_STREAM, fileUri)
					putExtra(
						Intent.EXTRA_SUBJECT,
						context.getString(extraSubject)
					)
					putExtra(
						Intent.EXTRA_TEXT,
						context.getString(extraText)
					)
				}

				return Intent.createChooser(intentShareFile, context.getString(dialogText))
			}
			return null
		}

		fun getEmptyFuelStation(): FuelStation {
			return FuelStation(0, "", "", "", "",
				"", "", "", "0", "0",
				"0", "0","0", "0",
				"0", "0", "0", "0",
				"0", "0", "0", "0",
				"", "", "", "", "", "",
				0, 0, 0, 0)
		}
	}
}
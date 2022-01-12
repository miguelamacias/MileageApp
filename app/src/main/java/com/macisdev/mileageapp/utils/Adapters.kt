package com.macisdev.mileageapp.utils

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.macisdev.mileageapp.R
import com.macisdev.mileageapp.model.Mileage
import java.util.*

class Adapters {
	class MileageAdapter : ListAdapter<Mileage, MileageAdapter.MileageViewHolder>(MileageDiffCallback) {

		inner class MileageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
			private var currentMileage: Mileage? = null
			private val mileageContentTextView: TextView = view.findViewById(R.id.mileage_content_text_view)
			private val dateContentTextView: TextView = view.findViewById(R.id.date_content_text_view)
			private val kilometresContentTextView: TextView = view.findViewById(R.id.kilometres_content_text_view)
			private val litresContentTextView: TextView = view.findViewById(R.id.litres_content_text_view)
			private val notesTextView: TextView = view.findViewById(R.id.notes_text_view)

			fun bindData(mileage: Mileage) {
				currentMileage = mileage

				mileageContentTextView.text = String.format(Locale.getDefault(), "%.2f", mileage.mileage)
				dateContentTextView.text = DateFormat.format("dd/MM/yy", mileage.date)
				kilometresContentTextView.text = mileage.kilometres.toString()
				litresContentTextView.text = mileage.litres.toString()
				notesTextView.text = mileage.notes
			}
		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MileageAdapter.MileageViewHolder {
			val layoutInflater = LayoutInflater.from(parent.context)
			return MileageViewHolder(layoutInflater.inflate(R.layout.list_item_mileage, parent, false))
		}

		override fun onBindViewHolder(holder: MileageAdapter.MileageViewHolder, position: Int) {
			holder.bindData(currentList[position])
		}
	}

	object MileageDiffCallback : DiffUtil.ItemCallback<Mileage>() {
		override fun areItemsTheSame(oldItem: Mileage, newItem: Mileage): Boolean {
			return oldItem.id == newItem.id
		}

		override fun areContentsTheSame(oldItem: Mileage, newItem: Mileage): Boolean {
			return oldItem == newItem
		}
	}

	class IconAdapter(context: Context, textViewResourceId: Int, val objects: List<Int>) :
		ArrayAdapter<Int>(context, textViewResourceId, objects) {

		override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
			return getCustomView(position, parent)
		}

		override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
			return getCustomView(position, parent)
		}

		fun getCustomView(position: Int, parent: ViewGroup): View {
			val layoutInflater = LayoutInflater.from(parent.context)
			val iconRow = layoutInflater.inflate(R.layout.spinner_item_icon_vehicle, parent, false)
			val iconHolder = iconRow.findViewById<ImageView>(R.id.icon_holder)
			iconHolder.setImageResource(objects[position])
			return iconRow
		}

	}

	class ColorAdapter(context: Context, textViewResourceId: Int, val objects: List<Int>) :
		ArrayAdapter<Int>(context, textViewResourceId, objects) {

		override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
			return getCustomView(position, parent)
		}

		override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
			return getCustomView(position, parent)
		}

		fun getCustomView(position: Int, parent: ViewGroup): View {
			val layoutInflater = LayoutInflater.from(parent.context)
			val colorRow = layoutInflater.inflate(R.layout.spinner_item_color_vehicle, parent, false)
			val colorHolder = colorRow.findViewById<View>(R.id.color_holder)
			colorHolder.setBackgroundResource(objects[position])
			return colorRow
		}
	}
}
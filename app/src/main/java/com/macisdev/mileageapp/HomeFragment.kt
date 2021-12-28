package com.macisdev.mileageapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.macisdev.mileageapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
	private lateinit var gui: FragmentHomeBinding

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		gui = FragmentHomeBinding.inflate(inflater, container, false)
		return gui.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		gui.addButton.setOnClickListener {
			val directions = HomeFragmentDirections.actionHomeFragmentToAddMileageFragment()
			findNavController().navigate(directions)
		}

		gui.listButton.setOnClickListener {
			val directions = HomeFragmentDirections.actionHomeFragmentToMileageListFragment("8054FDG")
			findNavController().navigate(directions)
		}

		gui.quickButton.setOnClickListener {
			val directions = HomeFragmentDirections.actionHomeFragmentToQuickMileageFragment()
			findNavController().navigate(directions)
		}
	}
}
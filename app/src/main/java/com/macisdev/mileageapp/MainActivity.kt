package com.macisdev.mileageapp

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.macisdev.mileageapp.databinding.ActivityMainBinding
import com.macisdev.mileageapp.model.Vehicle


class MainActivity : AppCompatActivity(), AddVehicleFragment.Callbacks {
	companion object {
		const val TAG = "MileageAppTag"
	}

	private lateinit var gui: ActivityMainBinding

	private lateinit var appBarConfiguration: AppBarConfiguration
	private lateinit var navController: NavController

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		gui = ActivityMainBinding.inflate(layoutInflater)
		setContentView(gui.root)

		setSupportActionBar(gui.toolbar)

		//Drawer code
		//Get the fragment where the others fragments are shown
		val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
		//Get the controller of the navigation component
		navController = navHostFragment.navController
		//Bind each menu item to a destination with the same ID
		gui.navView.setupWithNavController(navController)
		//Set the top-level fragment
		appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment), gui.drawerLayout)
		//Binds the drawer to the app bar
		setupActionBarWithNavController(navController, appBarConfiguration)

		//Add vehicles to the navigation drawer menu
		MileageRepository.get().getVehicles().forEach { vehicle ->
			gui.navView.menu.getItem(0).subMenu
				.add(R.id.vehicles_group, Menu.NONE, 0, vehicle.plateNumber)
				.setIcon(R.drawable.ic_baseline_directions_car_24)
				.setOnMenuItemClickListener {
					val directions = HomeFragmentDirections.actionHomeFragmentToMileageListFragment(vehicle.plateNumber)
					navController.navigate(directions)
					gui.drawerLayout.closeDrawers()
					true
				}
		}
	}

	//Used by the navigationUI component (drawer)
	override fun onSupportNavigateUp(): Boolean {
		val navController = findNavController(R.id.nav_host_fragment)
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}

	override fun onVehicleAdded(vehicle: Vehicle) {
		gui.navView.menu.getItem(0).subMenu
			.add(R.id.vehicles_group, Menu.NONE, 0, vehicle.plateNumber)
			.setIcon(R.drawable.ic_baseline_directions_car_24)
			.setOnMenuItemClickListener {
				val directions = HomeFragmentDirections.actionHomeFragmentToMileageListFragment(vehicle.plateNumber)
				navController.navigate(directions)
				gui.drawerLayout.closeDrawers()
				true
			}
	}
}
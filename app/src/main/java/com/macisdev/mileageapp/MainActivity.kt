package com.macisdev.mileageapp

import android.os.Bundle
import android.view.Menu
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.macisdev.mileageapp.databinding.ActivityMainBinding
import com.macisdev.mileageapp.viewModels.MainActivityViewModel
import de.raphaelebner.roomdatabasebackup.core.RoomBackup

class MainActivity : AppCompatActivity() {
	companion object {
		@Suppress("unused")
		const val TAG = "MileageAppTag"
	}

	private lateinit var gui: ActivityMainBinding

	private lateinit var firebaseAnalytics: FirebaseAnalytics

	lateinit var roomBackup: RoomBackup

	private lateinit var appBarConfiguration: AppBarConfiguration
	private lateinit var navController: NavController
	private val mainActivityVM: MainActivityViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		firebaseAnalytics = Firebase.analytics
		roomBackup = RoomBackup(this)
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

		gui.navView.itemIconTintList = null //Allows tinting of vehicles icons

		loadVehiclesToNavDrawer()
	}

	private fun loadVehiclesToNavDrawer() {
		mainActivityVM.vehiclesList.observe(this) { vehiclesList ->
			val vehiclesMenu = gui.navView.menu.getItem(0).subMenu
			vehiclesMenu.clear()
			vehiclesList.forEach { vehicle ->
				val iconId = resources.getIdentifier(vehicle.icon, "drawable", packageName)
				val colorId = resources.getIdentifier(vehicle.color, "color", packageName)

				val icon = ContextCompat.getDrawable(this, iconId)
				icon?.colorFilter = BlendModeColorFilterCompat
					.createBlendModeColorFilterCompat(getColor(colorId), BlendModeCompat.SRC_ATOP)

				vehiclesMenu
					.add(R.id.vehicles_group, Menu.NONE, 0,
						"${vehicle.plateNumber} - ${vehicle.maker} ${vehicle.model}")
					.setIcon(icon)
					.setOnMenuItemClickListener {
						val directions = HomeFragmentDirections.
						actionHomeFragmentToMileageListFragment(vehicle.plateNumber, vehicle.maker, vehicle.model)
						navController.navigate(directions)
						gui.drawerLayout.closeDrawers()
						true
					}
			}
			vehiclesMenu
				.add(R.id.vehicles_group, Menu.NONE, 0, getString(R.string.add_vehicle))
				.setIcon(R.drawable.ic_add_circle)
				.setOnMenuItemClickListener {
					val directions = HomeFragmentDirections
						.actionHomeFragmentToAddVehicleFragment(false, "")
					navController.navigate(directions)
					true
				}
		}
	}

	//Used by the navigationUI component (drawer)
	override fun onSupportNavigateUp(): Boolean {
		val navController = findNavController(R.id.nav_host_fragment)
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}
}
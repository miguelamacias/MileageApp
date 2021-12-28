package com.macisdev.mileageapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.macisdev.mileageapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
	companion object {
		const val TAG = "MileageAppTag"
	}

	private lateinit var gui: ActivityMainBinding

	private lateinit var appBarConfiguration: AppBarConfiguration

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		gui = ActivityMainBinding.inflate(layoutInflater)
		setContentView(gui.root)

		setSupportActionBar(gui.toolbar)

		//Drawer code
		//Get the fragment where the others fragments are shown
		val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
		//Get the controller of the navigation component
		val navController = navHostFragment.navController
		//Set the top-level fragment
		appBarConfiguration = AppBarConfiguration(setOf(R.id.homeFragment), gui.drawerLayout)
		//Binds the drawer to the app bar
		setupActionBarWithNavController(navController, appBarConfiguration)
	}

	//Used by the navigationUI component (drawer)
	override fun onSupportNavigateUp(): Boolean {
		val navController = findNavController(R.id.nav_host_fragment)
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}
}
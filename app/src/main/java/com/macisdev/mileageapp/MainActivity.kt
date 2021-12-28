package com.macisdev.mileageapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
	companion object {
		const val TAG = "MileageAppTag"
	}


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)


		/*val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

		if (currentFragment == null) {
			val fragment = HomeFragment.newInstance()
			//val fragment = AddMileageFragment.newInstance()
			//val fragment = MileageListFragment.newInstance("8054FDG")
			//val fragment = QuickMileageFragment.newInstance()
			supportFragmentManager
				.beginTransaction()
				.add(R.id.fragment_container, fragment)
				.commit()
		}*/
	}
}
package com.macisdev.mileageapp.database

import android.app.Application

@Suppress("unused")
class MileageAppApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		AppDataRepository.initialize(this)
	}
}
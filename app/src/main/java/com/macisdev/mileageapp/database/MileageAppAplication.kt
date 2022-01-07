package com.macisdev.mileageapp.database

import android.app.Application

@Suppress("unused")
class MileageAppAplication : Application() {
	override fun onCreate() {
		super.onCreate()
		MileageRepository.initialize(this)
	}
}
package com.macisdev.mileageapp

import android.app.Application

class MileageAppAplication : Application() {
	override fun onCreate() {
		super.onCreate()
		MileageRepository.initialize(this)
	}
}
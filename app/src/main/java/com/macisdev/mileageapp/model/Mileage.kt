package com.macisdev.mileageapp.model

import java.util.*

data class Mileage(val vehicle: Vehicle,
				   val date: Date,
				   val mileage: Double,
				   val kilometres: Double,
				   val litres: Double,
				   val notes: String = "")

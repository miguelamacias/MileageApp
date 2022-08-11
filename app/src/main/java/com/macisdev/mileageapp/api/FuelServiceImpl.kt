package com.macisdev.mileageapp.api

import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macisdev.mileageapp.MainActivity
import com.macisdev.mileageapp.api.fuel.FuelResponse
import com.macisdev.mileageapp.api.fuel.FuelServiceCalls
import com.macisdev.mileageapp.api.fuel.FuelStation
import com.macisdev.mileageapp.database.API_STATUS_OK
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class FuelServiceImpl {
    private val fuelAPI: FuelServiceCalls

    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        fuelAPI = retrofit.create(FuelServiceCalls::class.java)
    }

    fun getCityCodeByZipCode(zip: String): LiveData<Int> {
        val cityCode: MutableLiveData<Int> = MutableLiveData()

        fuelAPI.getAllFuelStations().enqueue(object : Callback<FuelResponse> {
            override fun onResponse(call: Call<FuelResponse>, response: Response<FuelResponse>) {
                val responseBody = response.body()
                var codeFound = false

                if (responseBody?.resultadoConsulta == API_STATUS_OK) {
                    responseBody.listaEESSPrecio.takeWhile { !codeFound }.forEach { fuelStation ->
                        if (fuelStation.CP.toString() == zip) {
                            cityCode.value = fuelStation.iDMunicipio
                            codeFound = true
                        }
                    }
                }

                if (!codeFound) {
                    cityCode.value = 0
                }
            }

            override fun onFailure(call: Call<FuelResponse>, t: Throwable) {
                cityCode.value = 0
                Log.e(MainActivity.TAG, "Error calling fuel service in getCityCodeByZipCode")
            }

        })

        return cityCode
    }

    fun getFuelStationsByCityCode(cityCode: Int): LiveData<List<FuelStation>> {
        val fuelStationsList: MutableLiveData<List<FuelStation>> = MutableLiveData()

        fuelAPI.getByCity(cityCode).enqueue(object : Callback<FuelResponse> {
            override fun onResponse(call: Call<FuelResponse>, response: Response<FuelResponse>) {
                val responseBody = response.body()

                if (responseBody?.resultadoConsulta == API_STATUS_OK) {
                    fuelStationsList.value = responseBody.listaEESSPrecio.filter { it.tipoVenta == "P" }
                }
            }

            override fun onFailure(call: Call<FuelResponse>, t: Throwable) {
                fuelStationsList.value = emptyList()
                Log.e(MainActivity.TAG, "Error calling fuel service in getFuelStationsByCityCode()")
            }

        })

        return fuelStationsList
    }

    fun getFuelStationById(cityCode: Int, stationId: Int): LiveData<FuelStation> {
        val station: MutableLiveData<FuelStation> = MutableLiveData()

        fuelAPI.getByCity(cityCode).enqueue(object : Callback<FuelResponse> {
            override fun onResponse(call: Call<FuelResponse>, response: Response<FuelResponse>) {
                val responseBody = response.body()

                if (responseBody?.resultadoConsulta == API_STATUS_OK) {
                    var codeFound = false

                    responseBody.listaEESSPrecio.takeWhile { !codeFound }.forEach { fuelStation ->
                        if (fuelStation.iDEESS == stationId) {
                            fuelStation.creationDate = responseBody.fecha
                            station.value = fuelStation
                            codeFound = true
                        }
                    }
                    if (!codeFound) {
                        station.value = FuelStation.getEmptyFuelStation()
                    }
                } else {
                    station.value = FuelStation.getEmptyFuelStation()
                }
            }

            override fun onFailure(call: Call<FuelResponse>, t: Throwable) {
                station.value = FuelStation.getEmptyFuelStation()
                Log.e(MainActivity.TAG, "Error calling fuel service in getFuelStationById()")
            }
        })

        return station
    }

    fun getHistoricalData(date: Date, cityCode: Int, stationId: Int): LiveData<FuelStation> {
        val station: MutableLiveData<FuelStation> = MutableLiveData()
        val formattedDate = DateFormat.format("dd-MM-yyyy", date).toString()

        fuelAPI.getHistoricalData(formattedDate, cityCode).enqueue(object : Callback<FuelResponse> {
            override fun onResponse(call: Call<FuelResponse>, response: Response<FuelResponse>) {
                val responseBody = response.body()

                if (responseBody?.resultadoConsulta == API_STATUS_OK) {
                    var codeFound = false

                    responseBody.listaEESSPrecio.takeWhile { !codeFound }.forEach { fuelStation ->
                        if (fuelStation.iDEESS == stationId) {
                            station.value = fuelStation
                            codeFound = true
                        }
                    }
                    if (!codeFound) {
                        station.value = FuelStation.getEmptyFuelStation()
                    }
                } else {
                    station.value = FuelStation.getEmptyFuelStation()
                }
            }

            override fun onFailure(call: Call<FuelResponse>, t: Throwable) {
                station.value = FuelStation.getEmptyFuelStation()
                Log.e(MainActivity.TAG, "Error calling fuel service in getHistoricalData()")
            }
        })

        return station
    }
}
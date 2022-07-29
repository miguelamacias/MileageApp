package com.macisdev.mileageapp.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.macisdev.mileageapp.MainActivity
import com.macisdev.mileageapp.api.fuel.FuelResponse
import com.macisdev.mileageapp.api.fuel.FuelServiceCalls
import com.macisdev.mileageapp.api.fuel.ListaEESSPrecio
import com.macisdev.mileageapp.database.API_STATUS_OK
import com.macisdev.mileageapp.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FuelServiceImpl {
    fun getCityCodeByZipCode(zip: String): LiveData<Int> {
        val cityCode: MutableLiveData<Int> = MutableLiveData()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val fuelAPI = retrofit.create(FuelServiceCalls::class.java)

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

    fun getFuelStationsByCityCode(cityCode: Int): LiveData<List<ListaEESSPrecio>> {
        val fuelStationsList: MutableLiveData<List<ListaEESSPrecio>> = MutableLiveData()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val fuelAPI = retrofit.create(FuelServiceCalls::class.java)

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

    fun getPreferredFuelStation(cityCode: Int, stationId: Int): LiveData<ListaEESSPrecio> {
        val station: MutableLiveData<ListaEESSPrecio> = MutableLiveData()

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/PreciosCarburantes/EstacionesTerrestres/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val fuelAPI = retrofit.create(FuelServiceCalls::class.java)

        fuelAPI.getByCity(cityCode).enqueue(object : Callback<FuelResponse> {
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
                        station.value = Utils.getEmptyFuelStation()
                    }
                } else {
                    station.value = Utils.getEmptyFuelStation()
                }
            }

            override fun onFailure(call: Call<FuelResponse>, t: Throwable) {
                station.value = Utils.getEmptyFuelStation()
                Log.e(MainActivity.TAG, "Error calling fuel service in getPreferredFuelStation()")
            }
        })

        return station
    }
}
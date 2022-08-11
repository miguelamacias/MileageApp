package com.macisdev.mileageapp.api.fuel

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FuelServiceCalls {
    @GET("EstacionesTerrestres/")
    fun getAllFuelStations(): Call<FuelResponse>

    @GET("EstacionesTerrestres/FiltroMunicipio/{city}")
    fun getByCity(
        @Path("city") cityCode : Int
    ): Call<FuelResponse>

    @GET("EstacionesTerrestresHist/FiltroMunicipio/{date}/{city}")
    fun getHistoricalData(
        @Path("date") date: String,
        @Path("city") cityCode : Int
    ): Call<FuelResponse>
}
package com.macisdev.mileageapp.api.fuel

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FuelServiceCalls {
    @GET("./")
    fun getAllFuelPosts(): Call<FuelResponse>

    @GET("FiltroMunicipio/{city}")
    fun getByCity(
        @Path("city") cityCode : Int
    ): Call<FuelResponse>
}
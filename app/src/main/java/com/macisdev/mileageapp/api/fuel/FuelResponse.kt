@file:Suppress("SpellCheckingInspection")
package com.macisdev.mileageapp.api.fuel

import com.google.gson.annotations.SerializedName

data class FuelResponse (

    @SerializedName("Fecha") val fecha : String,
    @SerializedName("ListaEESSPrecio") val listaEESSPrecio : List<ListaEESSPrecio>,
    @SerializedName("Nota") val nota : String,
    @SerializedName("ResultadoConsulta") val resultadoConsulta : String
)
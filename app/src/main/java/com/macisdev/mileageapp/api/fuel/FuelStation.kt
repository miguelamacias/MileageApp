@file:Suppress("SpellCheckingInspection")
package com.macisdev.mileageapp.api.fuel

import com.google.gson.annotations.SerializedName

data class FuelStation (

	@SerializedName("C.P.") val CP : Int,
	@SerializedName("Dirección") val direccion : String,
	@SerializedName("Horario") val horario : String,
	@SerializedName("Latitud") val latitud : String,
	@SerializedName("Localidad") val localidad : String,
	@SerializedName("Longitud (WGS84)") val longitudWGS84 : String,
	@SerializedName("Margen") val margen : String,
	@SerializedName("Municipio") val municipio : String,
	@SerializedName("Precio Biodiesel") val precioBiodiesel : String,
	@SerializedName("Precio Bioetanol") val precioBioetanol : String,
	@SerializedName("Precio Gas Natural Comprimido") val precioGasNaturalComprimido : String,
	@SerializedName("Precio Gas Natural Licuado") val precioGasNaturalLicuado : String,
	@SerializedName("Precio Gases licuados del petróleo") val precioGasesLicuadosDelPetroleo : String,
	@SerializedName("Precio Gasoleo A") var precioGasoleoA : String,
	@SerializedName("Precio Gasoleo B") val precioGasoleoB : String,
	@SerializedName("Precio Gasoleo Premium") val precioGasoleoPremium : String,
	@SerializedName("Precio Gasolina 95 E10") val precioGasolina95E10 : String,
	@SerializedName("Precio Gasolina 95 E5") var precioGasolina95E5 : String,
	@SerializedName("Precio Gasolina 95 E5 Premium") val precioGasolina95E5Premium : String,
	@SerializedName("Precio Gasolina 98 E10") val precioGasolina98E10 : String,
	@SerializedName("Precio Gasolina 98 E5") val precioGasolina98E5 : String,
	@SerializedName("Precio Hidrogeno") val precioHidrogeno : String,
	@SerializedName("Provincia") val provincia : String,
	@SerializedName("Remisión") val remision : String,
	@SerializedName("Rótulo") val rotulo : String,
	@SerializedName("Tipo Venta") val tipoVenta : String,
	@SerializedName("% BioEtanol") val porcentajeBioEtanol : String,
	@SerializedName("% Éster metílico") val porcentajeEsterMetilico : String,
	@SerializedName("IDEESS") val iDEESS : Int,
	@SerializedName("IDMunicipio") val iDMunicipio : Int,
	@SerializedName("IDProvincia") val iDProvincia : Int,
	@SerializedName("IDCCAA") val iDCCAA : Int,
	var creationDate: String = "",
	var cheapestDiesel: Boolean = false,
	var cheapestPetrol: Boolean = false,
	var mostExpensiveDiesel: Boolean = false,
	var mostExpensivePetrol: Boolean = false
)
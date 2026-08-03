package com.example.proyectwin.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Ficha(
    val id: Int,
    val codigo: String,
    val programa: String,
    val estado: String = ClassGroupStatus.ACTIVO.value,
    val instructorId: Int? = null,
    val instructorName: String? = null,
    val createdAt: String? = null,
    val estudiantes: List<GeneralUser> = emptyList()
) {
    val classGroupStatus: ClassGroupStatus get() = ClassGroupStatus.fromValue(estado)
    val statusDisplay: String get() = when (classGroupStatus) {
        ClassGroupStatus.ACTIVO -> "Activo"
        ClassGroupStatus.INACTIVO -> "Inactivo"
        ClassGroupStatus.FINALIZADO -> "Finalizado"
    }

    companion object {
        val fichasValidas = setOf(
            "FT-2692701", "FT-2771109", "FT-2823412", "FT-2874441",
            "FT-2902005", "FT-2993476", "FT-3039845", "FT-3067030",
            "FT-3114059", "FT-3184978", "FT-3258694", "FT-3309264",
            "FT-2345678", "FT-2412345", "FT-2510101", "FT-2556789",
            "FT-2622222", "FT-2644444", "FT-2678901", "FT-2711111",
            "FT-2733333", "FT-2755555", "FT-2789999", "FT-2812345",
            "FT-2834567", "FT-2856789", "FT-2889012", "FT-2923456",
            "FT-2945678", "FT-2967890", "FT-2989012", "FT-3001234",
            "FT-3023456", "FT-3045678", "FT-3087890", "FT-3109012",
            "FT-3131234", "FT-3153456", "FT-3175678", "FT-3207890",
            "FT-3229012", "FT-3271234"
        )

        fun esCodigoValido(codigo: String): Boolean = fichasValidas.contains(codigo)

        fun generarCodigo(): String = fichasValidas.random()

        fun generarCodigoLibre(codigosUsados: Collection<String>): String {
            val disponibles = fichasValidas - codigosUsados.toSet()
            return if (disponibles.isEmpty()) fichasValidas.random() else disponibles.random()
        }
    }
}

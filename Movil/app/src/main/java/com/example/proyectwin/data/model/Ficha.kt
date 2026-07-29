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
        private val fichasValidas = setOf(
            "FT-2692701", "FT-2771109", "FT-2823412", "FT-2874441",
            "FT-2902005", "FT-2993476", "FT-3039845", "FT-3067030",
            "FT-3114059", "FT-3184978", "FT-3258694", "FT-3309264"
        )

        fun esCodigoValido(codigo: String): Boolean = fichasValidas.contains(codigo)

        fun generarCodigo(): String {
            val numero = (100000..999999).random()
            return "FT-$numero"
        }
    }
}

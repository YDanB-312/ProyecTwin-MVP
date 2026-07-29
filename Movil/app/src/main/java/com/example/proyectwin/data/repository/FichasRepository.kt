package com.example.proyectwin.data.repository

import com.example.proyectwin.data.mock.MockDataProvider
import com.example.proyectwin.data.model.Ficha
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class FichasRepository {

    fun getAllFichas(): Flow<List<Ficha>> = flow {
        emit(MockDataProvider.getAllFichas())
    }

    fun getActiveFichas(): Flow<List<Ficha>> = flow {
        emit(MockDataProvider.getActiveFichas())
    }

    fun getFichaById(id: Int): Flow<Ficha?> = flow {
        emit(MockDataProvider.findFichaById(id))
    }

    fun getFichaByCodigo(codigo: String): Flow<Ficha?> = flow {
        emit(MockDataProvider.findFichaByCodigo(codigo))
    }

    fun esCodigoValido(codigo: String): Boolean = Ficha.esCodigoValido(codigo)

    fun generarCodigo(): String = Ficha.generarCodigo()
}

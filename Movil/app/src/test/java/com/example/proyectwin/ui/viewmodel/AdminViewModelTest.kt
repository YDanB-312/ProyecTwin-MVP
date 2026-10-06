package com.example.proyectwin.ui.viewmodel

import com.example.proyectwin.data.model.Project
import com.example.proyectwin.data.model.Similarity
import com.example.proyectwin.domain.repository.BugReportsRepository
import com.example.proyectwin.domain.repository.ProjectsRepository
import com.example.proyectwin.domain.repository.SimilaritiesRepository
import com.example.proyectwin.domain.repository.UsersRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * La pantalla de Similitudes del admin completa el listado global (solo pares
 * con ambos lados aprobados) con los pares de cada propuesta. Esta prueba fija
 * ese contrato: todos los pares recuperables por la API quedan en la lista,
 * sin duplicados y con los vigentes primero.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `la agregacion reune los pares de todas las propuestas y deduplica`() = runTest(dispatcher) {
        val users = mockk<UsersRepository>()
        val projects = mockk<ProjectsRepository>()
        val bugs = mockk<BugReportsRepository>()
        val sims = mockk<SimilaritiesRepository>()

        coEvery { users.listar(any(), any(), any(), any()) } returns Result.success(emptyList())
        coEvery { bugs.listar() } returns Result.success(emptyList())
        coEvery { projects.listar(any(), any(), any(), any()) } returns Result.success(
            (1..4).map { Project(id = it, title = "Propuesta $it") },
        )

        // Par "ambos aprobados" (visible en el listado global) + dos pares que
        // solo se recuperan consultando cada propuesta (contraparte aprobada e
        // histórica). El par 1 debe aparecer una sola vez aunque toque dos
        // propuestas distintas.
        val global = Similarity(id = 3, projectId1 = 2, projectId2 = 3, vigente = true)
        val porPropuesta = Similarity(id = 1, projectId1 = 1, projectId2 = 4, vigente = true)
        val historica = Similarity(id = 8, projectId1 = 2, projectId2 = 4, vigente = false)

        coEvery { sims.listar(any(), any(), any(), any(), any(), any()) } answers {
            val proyectoId = firstArg<Int?>()
            val historial = secondArg<Boolean?>()
            val encontrados = listOf(global, porPropuesta, historica).filter { par ->
                (par.projectId1 == proyectoId || par.projectId2 == proyectoId) &&
                    if (historial == true) !par.vigente else par.vigente
            }
            Result.success(encontrados)
        }
        coEvery { sims.listar() } returns Result.success(listOf(global))

        val vm = AdminViewModel(users, projects, bugs, sims)
        advanceUntilIdle()
        vm.cargarSimilitudesCompletas()
        advanceUntilIdle()

        assertEquals(
            listOf(3, 1, 8),
            vm.similitudesCompletas.value?.map { it.id },
        )
    }
}

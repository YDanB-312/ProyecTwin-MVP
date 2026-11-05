package com.example.proyectwin.ui.viewmodel

import com.example.proyectwin.data.api.ApiException
import com.example.proyectwin.data.model.AuditLog
import com.example.proyectwin.data.model.PaginaAuditoria
import com.example.proyectwin.domain.repository.AuditRepository
import com.example.proyectwin.domain.repository.UsersRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Bitácora admin con paginación y filtros resueltos por el servidor. */
@OptIn(ExperimentalCoroutinesApi::class)
class BitacoraViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val audit = mockk<AuditRepository>(relaxed = true)
    private val users = mockk<UsersRepository>(relaxed = true)

    private fun viewModel() = BitacoraViewModel(audit, users)

    private fun registro(id: Int) = AuditLog(
        id = id,
        accion = "crear_usuario",
        createdAt = "2026-10-01T00:00:00.000000Z",
    )

    private fun pagina(
        ids: List<Int>,
        pagina: Int = 1,
        totalPaginas: Int = 1,
        total: Int = ids.size,
    ) = PaginaAuditoria(
        registros = ids.map(::registro),
        pagina = pagina,
        totalPaginas = totalPaginas,
        totalRegistros = total,
    )

    private fun datosDe(vm: BitacoraViewModel): BitacoraUiState.Success =
        vm.uiState.value as BitacoraUiState.Success

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        coEvery {
            audit.listarPaginado(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(pagina(emptyList()))
        coEvery { users.listar() } returns Result.success(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `carga inicial pide la primera pagina al servidor`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        coVerify(exactly = 1) {
            audit.listarPaginado(1, REGISTROS_POR_PAGINA, null, null, null, null, null, null, null, null)
        }
        assertTrue(vm.uiState.value is BitacoraUiState.Success)
    }

    @Test
    fun `filtros viajan al servidor y vuelven a la primera pagina`() = runTest(dispatcher) {
        val vm = viewModel()
        advanceUntilIdle()

        vm.irAPagina(3)
        advanceUntilIdle()
        vm.load(
            accion = "crear_usuario",
            entidad = "general_users",
            idUsuario = 7,
            desde = "2026-10-01",
            hasta = "2026-10-31",
            buscar = "ana",
            rol = "admin",
            orden = "antiguos",
        )
        advanceUntilIdle()

        coVerify(exactly = 1) {
            audit.listarPaginado(
                1,
                REGISTROS_POR_PAGINA,
                "ana",
                "crear_usuario",
                "general_users",
                7,
                "admin",
                "2026-10-01",
                "2026-10-31",
                "antiguos",
            )
        }
        assertEquals("ana", datosDe(vm).filtros.buscar)
        assertEquals(1, datosDe(vm).pagina)
    }

    @Test
    fun `paginar consulta la pagina siguiente sin perder filtros`() = runTest(dispatcher) {
        coEvery {
            audit.listarPaginado(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(pagina((9..16).toList(), pagina = 1, totalPaginas = 3, total = 20))
        val vm = viewModel()
        advanceUntilIdle()
        vm.load(idUsuario = 7)
        advanceUntilIdle()

        coEvery {
            audit.listarPaginado(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(pagina((9..16).toList(), pagina = 2, totalPaginas = 3, total = 20))
        vm.irAPagina(2)
        advanceUntilIdle()

        val datos = datosDe(vm)
        assertEquals(2, datos.pagina)
        assertEquals(3, datos.totalPaginas)
        assertEquals((9..16).toList(), datos.visibles.map { it.id })
        assertEquals(7, datos.filtros.idUsuario)
        coVerify(exactly = 1) {
            audit.listarPaginado(2, REGISTROS_POR_PAGINA, null, null, null, 7, null, null, null, null)
        }
    }

    @Test
    fun `error es recuperable manteniendo filtros`() = runTest(dispatcher) {
        coEvery {
            audit.listarPaginado(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(ApiException.Http(500, "Fallo temporal"))
        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value is BitacoraUiState.Error)

        coEvery {
            audit.listarPaginado(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(pagina(listOf(1, 2), total = 2))
        vm.load(accion = "crear_usuario")
        advanceUntilIdle()

        assertEquals(2, datosDe(vm).registros.size)
        assertEquals("crear_usuario", datosDe(vm).filtros.accion)
    }

    @Test
    fun `respuesta vieja no pisa un filtro mas reciente`() = runTest(dispatcher) {
        coEvery {
            audit.listarPaginado(1, REGISTROS_POR_PAGINA, null, null, null, null, null, null, null, null)
        } coAnswers {
            delay(100)
            Result.success(pagina(listOf(1)))
        }
        coEvery {
            audit.listarPaginado(1, REGISTROS_POR_PAGINA, null, null, null, 7, null, null, null, null)
        } returns Result.success(pagina(listOf(3, 4, 5)))

        val vm = viewModel()
        runCurrent()
        vm.load(idUsuario = 7)
        advanceUntilIdle()

        assertEquals(listOf(3, 4, 5), datosDe(vm).registros.map { it.id })
        assertEquals(7, datosDe(vm).filtros.idUsuario)
    }
}

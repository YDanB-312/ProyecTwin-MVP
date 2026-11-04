package com.example.proyectwin.ui.screens.admin

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyectwin.ui.theme.ProyecTwinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Smoke test instrumentado de [UserCard]: datos y acciones visibles. */
@RunWith(AndroidJUnit4::class)
class UserCardSmokeTest {

    @get:Rule
    val rule = createComposeRule()

    private val usuario = UserItem(
        id = 1,
        name = "Ana Prueba",
        document = "1234567890",
        email = "ana.prueba@example.com",
        role = "aprendiz",
        status = "activo",
    )

    @Test
    fun userCard_normal_muestraDatosYAccionesSinCheckbox() {
        rule.setContent {
            ProyecTwinTheme(darkTheme = false) {
                UserCard(user = usuario, onEdit = {}, onDelete = {})
            }
        }

        rule.onNodeWithText("Ana Prueba").assertIsDisplayed()
        rule.onNodeWithText("ana.prueba@example.com").assertIsDisplayed()
        rule.onNodeWithText("ACTIVO").assertIsDisplayed()
        rule.onNodeWithContentDescription("Editar").assertIsDisplayed()
        rule.onNodeWithContentDescription("Eliminar").assertIsDisplayed()
        rule.onAllNodes(isToggleable()).assertCountEquals(0)
    }
}

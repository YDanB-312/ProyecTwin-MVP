package com.example.proyectwin.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyectwin.ui.theme.ProyecTwinTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManualPdfProfileEntryTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun laEntradaDePerfilAbreElMismoVisorPdf() {
        rule.setContent {
            ProyecTwinTheme {
                ManualPdfProfileEntry()
            }
        }

        rule.onNodeWithText("Manual de usuario (PDF)")
            .assertIsDisplayed()
            .performClick()

        rule.onNodeWithContentDescription("Guardar manual").assertExists()
        rule.onNodeWithContentDescription("Compartir manual").assertExists()
        rule.onNodeWithContentDescription("Abrir manual").assertExists()
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithContentDescription("Página 1 del manual")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun laVisibilidadDelVisorSobreviveALaRestauracionDeEstado() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            ProyecTwinTheme {
                ManualPdfProfileEntry()
            }
        }

        rule.onNodeWithText("Manual de usuario (PDF)").performClick()
        restoration.emulateSavedInstanceStateRestore()

        rule.onNodeWithContentDescription("Guardar manual").assertExists()
    }
}

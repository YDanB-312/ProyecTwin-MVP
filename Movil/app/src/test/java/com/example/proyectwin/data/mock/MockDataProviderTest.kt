package com.example.proyectwin.data.mock

import org.junit.Assert.*
import org.junit.Test

class MockDataProviderTest {

    @Test
    fun users_containsThree() {
        assertEquals(3, MockDataProvider.users.size)
    }

    @Test
    fun findUserByEmail_returnsCorrect() {
        val user = MockDataProvider.findUserByEmail("instructor@test.com")
        assertNotNull(user)
        assertEquals("Carlos Instructor", user?.name)
    }

    @Test
    fun findUserByEmail_notFound_returnsNull() {
        assertNull(MockDataProvider.findUserByEmail("noexiste@test.com"))
    }

    @Test
    fun getAllFichas_contains12() {
        assertEquals(12, MockDataProvider.getAllFichas().size)
    }

    @Test
    fun getActiveFichas_returnsOnlyActive() {
        val active = MockDataProvider.getActiveFichas()
        assertTrue(active.all { it.estado == "activo" })
    }

    @Test
    fun getAllProjects_contains5() {
        assertEquals(5, MockDataProvider.getAllProjects().size)
    }

    @Test
    fun getProjectsByStudent_returnsOnlyStudentProjects() {
        val projects = MockDataProvider.getProjectsByStudent(2)
        assertTrue(projects.all { it.studentId == 2 })
        assertTrue(projects.isNotEmpty())
    }

    @Test
    fun getAllBugReports_contains3() {
        assertEquals(3, MockDataProvider.getAllBugReports().size)
    }

    @Test
    fun getAllSimilarities_contains2() {
        assertEquals(2, MockDataProvider.getAllSimilarities().size)
    }

    @Test
    fun getNotificationsByUser_returnsCorrect() {
        val notifs = MockDataProvider.getNotificationsByUser(1)
        assertTrue(notifs.isNotEmpty())
        assertTrue(notifs.all { it.userId == 1 })
    }
}

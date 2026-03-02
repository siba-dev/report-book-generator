package de.siba.reportbookgen.service

import de.siba.reportbookgen.model.ReportBookWeekJson
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReportBookDataServiceTest {
    @Test
    fun `no number test`() {
        val json = createDefaultJson()
        json.number = null
        assertThrows<IllegalStateException>("Could not determine week number!") {
            ReportBookDataService().mapData(json, createDefaultYearMap())
        }
    }

    private fun createDefaultJson(): ReportBookWeekJson = ReportBookWeekJson(
        number = 1,
        activity = listOf(),
        activity_hours = 16,
        teachings = listOf(),
        teachings_hours = 8,
        school = mapOf(),
        school_days = 1
    )

    private fun createDefaultYearMap(): Map<Int, LocalDate> = mapOf(1 to LocalDate(2025, 9, 1))

    @Test
    fun `invalid year map test`() {
        assertInvalidMapException(mapOf())
        assertInvalidMapException(mapOf(2 to LocalDate(2025, 9, 1)))
    }

    private fun assertInvalidMapException(yearMap: Map<Int, LocalDate>) {
        val e1 = assertFailsWith<IllegalArgumentException>("Unexpected success!") {
            ReportBookDataService().mapData(createDefaultJson(), yearMap)
        }
        assertEquals("Missing start date of year 1!", e1.message, "Wrong exception thrown!")
    }

    @Test
    fun `first week dates test`() {
        val data = ReportBookDataService().mapData(createDefaultJson(), createDefaultYearMap())
        assertEquals(LocalDate(2025, 9, 1), data.weekStart, "Wrong week start date!")
        assertEquals(LocalDate(2025, 9, 7), data.weekEnd, "Wrong week end date!")
    }

    @Test
    fun `second week dates test`() {
        val json = createDefaultJson()
        json.number = 2
        val data = ReportBookDataService().mapData(json, createDefaultYearMap())
        assertEquals(LocalDate(2025, 9, 8), data.weekStart, "Wrong week start date!")
        assertEquals(LocalDate(2025, 9, 14), data.weekEnd, "Wrong week end date!")
    }

    @Test
    fun `school hours test`() {
        val data = ReportBookDataService().mapData(createDefaultJson(), createDefaultYearMap())
        assertEquals(8, data.schoolHours, "Wrong school hours!")
    }

    @Test
    fun `week number override test`(@TempDir tmpDir: Path) {
        val tmpFilePath = tmpDir.resolve("1.json")

        copyResource("override-week.json", tmpFilePath)

        val data = ReportBookDataService().loadWeeklyData(tmpFilePath, createDefaultYearMap())
        assertEquals(5, data.weekNumber, "Used week data from filename instead of json content!")
    }

    private fun copyResource(resourcePath: String, path: Path) {
        javaClass.classLoader.getResourceAsStream(resourcePath)!!
            .use {
                Files.copy(it, path)
            }
    }

    @Test
    fun `week number from file test`(@TempDir tmpDir: Path) {
        val tmpFilePath = tmpDir.resolve("1.json")

        copyResource("no-override.json", tmpFilePath)

        val data = ReportBookDataService().loadWeeklyData(tmpFilePath, createDefaultYearMap())
        assertEquals(1, data.weekNumber)
    }

    @Test
    fun `no week number test`(@TempDir tmpDir: Path) {
        val tmpFilePath = tmpDir.resolve("no-num.json")

        copyResource("no-override.json", tmpFilePath)

        val e = assertFailsWith<IllegalStateException>("Unexpected success!") {
            ReportBookDataService().loadWeeklyData(tmpFilePath, createDefaultYearMap())
        }
        assertEquals("Could not determine week number!", e.message)
    }
}
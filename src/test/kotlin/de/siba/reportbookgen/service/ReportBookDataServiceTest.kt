package de.siba.reportbookgen.service

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportBookDataServiceTest {
    private val service = ReportBookDataService()

    @Test
    fun `no number test`() {
        val json = createDefaultJson()
        json.number = null

        assertFailsWithMessage<IllegalStateException>("Could not determine week number!") {
            service.mapData(json, createDefaultYearMap())
        }
    }

    @Test
    fun `invalid year map test`() {
        assertInvalidMapException(mapOf())
        assertInvalidMapException(mapOf(2 to LocalDate(2025, 9, 1)))
    }

    private fun assertInvalidMapException(yearMap: Map<Int, LocalDate>) {
        assertFailsWithMessage<IllegalArgumentException>("Missing start date of year 1!") {
            service.mapData(createDefaultJson(), yearMap)
        }
    }

    @Test
    fun `first week dates test`() {
        val data = service.mapData(createDefaultJson(), createDefaultYearMap())
        assertEquals(LocalDate(2025, 9, 1), data.weekStart, "Wrong week start date!")
        assertEquals(LocalDate(2025, 9, 7), data.weekEnd, "Wrong week end date!")
    }

    @Test
    fun `second week dates test`() {
        val json = createDefaultJson()
        json.number = 2
        val data = service.mapData(json, createDefaultYearMap())
        assertEquals(LocalDate(2025, 9, 8), data.weekStart, "Wrong week start date!")
        assertEquals(LocalDate(2025, 9, 14), data.weekEnd, "Wrong week end date!")
    }

    @Test
    fun `school hours test`() {
        val data = service.mapData(createDefaultJson(), createDefaultYearMap())
        assertEquals(8, data.schoolHours, "Wrong school hours!")
    }

    @Test
    fun `week number override test`(@TempDir tmpDir: Path) {
        val tmpFilePath = tmpDir.resolve("1.json")

        copyResource("override-week.json", tmpFilePath)

        val data = service.loadWeeklyData(tmpFilePath, createDefaultYearMap())
        assertEquals(5, data.weekNumber, "Used week data from filename instead of json content!")
    }

    @Test
    fun `week number from file test`(@TempDir tmpDir: Path) {
        val tmpFilePath = tmpDir.resolve("1.json")

        copyResource("no-override.json", tmpFilePath)

        val data = service.loadWeeklyData(tmpFilePath, createDefaultYearMap())
        assertEquals(1, data.weekNumber)
    }

    @Test
    fun `no week number test`(@TempDir tmpDir: Path) {
        val tmpFilePath = tmpDir.resolve("no-num.json")

        copyResource("no-override.json", tmpFilePath)

        assertFailsWithMessage<IllegalStateException>("Could not determine week number!") {
            service.loadWeeklyData(tmpFilePath, createDefaultYearMap())
        }
    }
}
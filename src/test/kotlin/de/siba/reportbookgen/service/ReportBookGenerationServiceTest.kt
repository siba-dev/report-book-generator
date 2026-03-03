package de.siba.reportbookgen.service

import de.siba.reportbookgen.model.ReportBookWeekData
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.createFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReportBookGenerationServiceTest {
    private val service = ReportBookGenerationService()

    @Test
    fun `copy word file test`(@TempDir inputDir: Path, @TempDir outputDir: Path) {
        val wordFile = inputDir.resolve("Nr1.docx")
            .createFile()
        val jsonFile = inputDir.resolve("1.json")
            .createFile()

        service.copyWordFiles(inputDir, outputDir)

        // Verify that only the docx file is copied to the output directory
        val outputFiles = outputDir.listDirectoryEntries()
        assertTrue("Word file was not copied.") {
            outputFiles.any { it.name == wordFile.name }
        }
        assertFalse("JSON file was copied but should not be.") {
            outputFiles.any { it.name == jsonFile.name }
        }
    }

    @Test
    fun `variable mapping with multiple values test`() {
        val data = createData(
            listOf("Creating new App", "Testing"),
            listOf("Instruction to Git", "Java"),
            mapOf("IT-T" to "Binary", "IT-S" to "Switches")
        )

        val result = service.mapVariables(data)

        assertEquals("5", result["number"])
        assertEquals("1", result["year"])
        assertEquals("01.09.2025", result["week_start"])
        assertEquals("03.09.2025", result["week_end"])
        assertEquals("Creating new App, Testing", result["activity"])
        assertEquals("16", result["activity_hours"])
        assertEquals("Instruction to Git, Java", result["teachings"])
        assertEquals("16", result["teachings_hours"])
        assertEquals("IT-T: Binary; IT-S: Switches", result["school"])
        assertEquals("8", result["school_hours"])
    }

    private fun createData(
        activity: List<String>, teachings: List<String>, school: Map<String, String>
    ): ReportBookWeekData {
        val data = ReportBookWeekData(
            weekNumber = 5,
            weekStart = LocalDate(2025, 9, 1),
            weekEnd = LocalDate(2025, 9, 3),
            year = 1,
            activity = activity,
            activityHours = 16,
            teachings = teachings,
            teachingsHours = 16,
            school = school,
            schoolHours = 8
        )
        return data
    }


    @Test
    fun `variable mapping with single value test`() {
        val data = createData(
            listOf("Creating new App"),
            listOf("Instruction to Git"),
            mapOf("IT-T" to "Binary")
        )

        val result = service.mapVariables(data)

        assertEquals("Creating new App", result["activity"])
        assertEquals("Instruction to Git", result["teachings"])
        assertEquals("IT-T: Binary", result["school"])
    }

    @Test
    fun `variable mapping with no value test`() {
        val data = createData(
            listOf(),
            listOf(),
            mapOf()
        )

        val result = service.mapVariables(data)

        assertEquals("", result["activity"])
        assertEquals("", result["teachings"])
        assertEquals("", result["school"])
    }
}
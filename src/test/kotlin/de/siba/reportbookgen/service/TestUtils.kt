package de.siba.reportbookgen.service

import de.siba.reportbookgen.model.ReportBookWeekJson
import kotlinx.datetime.LocalDate
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

fun createDefaultJson(): ReportBookWeekJson = ReportBookWeekJson(
    number = 1,
    activity = listOf(),
    activity_hours = 16,
    teachings = listOf(),
    teachings_hours = 8,
    school = mapOf(),
    school_days = 1
)

fun createDefaultYearMap(): Map<Int, LocalDate> = mapOf(1 to LocalDate(2025, 9, 1))

fun copyResource(resourcePath: String, path: Path) {
    Thread.currentThread().contextClassLoader.getResourceAsStream(resourcePath)!!
        .use {
            Files.copy(it, path)
        }
}

inline fun <reified T : Throwable> assertFailsWithMessage(expectedMessage: String? = null, block: () -> Unit) {
    val e = assertFailsWith(T::class, "Block did not fail.", block)
    if (expectedMessage != null) assertEquals(expectedMessage, e.message, "Mismatched error message!")
}
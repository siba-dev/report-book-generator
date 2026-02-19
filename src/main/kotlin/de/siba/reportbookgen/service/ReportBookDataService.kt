package de.siba.reportbookgen.service

import de.siba.reportbookgen.model.ReportBookWeekData
import de.siba.reportbookgen.model.ReportBookWeekJson
import de.siba.reportbookgen.service.ReportBookGenerationService.Companion.isJsonFile
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.*

class ReportBookDataService {
    fun loadAllWeeklyData(inputDir: Path, yearMap: Map<Int, LocalDate>): Map<Path, ReportBookWeekData> {
        val result = HashMap<Path, ReportBookWeekData>()

        inputDir.walk()
            .filter { it.isRegularFile() && isJsonFile(it) }
            .forEach { jsonPath ->
                result[jsonPath.absolute()] = loadWeeklyData(jsonPath, yearMap)
            }

        return result
    }

    private fun loadWeeklyData(jsonPath: Path, yearMap: Map<Int, LocalDate>): ReportBookWeekData {
        try {
            val jsonContent = jsonPath.readText()
            val jsonModel = Json.decodeFromString<ReportBookWeekJson>(jsonContent)

            if (jsonModel.number == null) {
                val fileNameWithoutExt = jsonPath.nameWithoutExtension
                jsonModel.number = fileNameWithoutExt.toInt()
            }

            return mapData(jsonModel, yearMap)
        } catch (e: Exception) {
            throw RuntimeException("Error loading '$jsonPath'.", e)
        }
    }

    private fun mapData(
        jsonModel: ReportBookWeekJson, yearMap: Map<Int, LocalDate>
    ): ReportBookWeekData {
        val weekNumber = jsonModel.number ?: throw IllegalStateException("Could not determine week number!")

        val startDate = yearMap[1] ?: throw IllegalArgumentException("Missing start date of year 1!")

        // Calculate week start (Monday) and end (Friday) dates
        val weekStart = startDate.plus(weekNumber - 1L, DateTimeUnit.WEEK)
        val weekEnd = weekStart.plus(4, DateTimeUnit.DAY) // Friday of the same week

        val year = yearMap.filter { it.value <= weekStart }
            .maxBy { it.value }.key

        return ReportBookWeekData(
            weekNumber,
            weekStart,
            weekEnd,
            year,
            jsonModel.activity,
            jsonModel.activity_hours,
            jsonModel.teachings,
            jsonModel.teachings_hours,
            jsonModel.school,
            jsonModel.school_days * 8
        )
    }
}
package de.siba.reportbookgen.service

import de.siba.reportbookgen.model.ReportBookData
import de.siba.reportbookgen.model.ReportBookWeekJson
import de.siba.reportbookgen.model.SchoolSubjectsJson
import kotlinx.serialization.json.Json
import org.docx4j.model.datastorage.migration.VariablePrepare
import org.docx4j.openpackaging.packages.WordprocessingMLPackage.load
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class ReportBookGenerationService {
    companion object {
        fun isJsonFile(file: File): Boolean = file.extension.equals("json", ignoreCase = true)
        fun isWordFile(file: File): Boolean = file.extension.equals("docx", ignoreCase = true)
    }

    fun generateFromJsons(
        inputDir: File,
        outputDir: File?,
        templateMap: Map<Int, File>
    ) {
        inputDir.walkTopDown()
            .filter { it.isFile && isJsonFile(it) }
            .forEach {
                process(it, outputDir, templateMap)
            }
    }

    fun process(
        jsonPath: File, outputDir: File?, templateMap: Map<Int, File>
    ) {
        try {
            process(jsonPath, outputDir, loadData(jsonPath), templateMap)
        } catch (e: Exception) {
            throw IllegalStateException("Error loading $jsonPath", e)
        }
    }

    private fun loadData(jsonPath: File): ReportBookData {
        val jsonContent = jsonPath.readText()
        val reportJson = Json.decodeFromString<ReportBookWeekJson>(jsonContent)

        if (reportJson.number == null) {
            val fileNameWithoutExt = jsonPath.nameWithoutExtension
            reportJson.number = fileNameWithoutExt.toInt()
        }

        return mapData(reportJson)
    }

    private fun mapData(reportJson: ReportBookWeekJson): ReportBookData {
        val startDate = LocalDate.of(2023, 8, 28)
        val weekNumber = reportJson.number!!

        // Calculate week start (Monday) and end (Friday) dates
        val weekStart = startDate.plusWeeks((weekNumber - 1).toLong())
        val weekEnd = weekStart.plusDays(6) // Friday of the same week

        var year = 1
        if (weekStart.isAfter(LocalDate.of(2024, 9, 1))) {
            year = 2
        }
        if (weekStart.isAfter(LocalDate.of(2025, 9, 1))) {
            year = 3
        }

        return ReportBookData(
            weekNumber,
            weekStart,
            weekEnd,
            year,
            reportJson.activity.joinToString(", "),
            reportJson.activity_hours,
            reportJson.teachings.joinToString(", "),
            reportJson.teachings_hours,
            formatSchoolSubjects(reportJson.school),
            reportJson.school_days * 8
        )
    }

    private fun formatSchoolSubjects(subjects: SchoolSubjectsJson): String {
        val subjects = listOf(
            "AWP" to subjects.awp,
            "IT-T" to subjects.itt,
            "IT-S" to subjects.its,
            "D" to subjects.d,
            "E" to subjects.e,
            "Eth" to subjects.eth,
            "PuG" to subjects.pug,
            "BP" to subjects.bp,
            "" to subjects.misc
        )

        return subjects.filter { (_, content) -> content.isNotBlank() }
            .joinToString("; ") { (subject, content) -> "$subject${if (subject.isNotEmpty()) ": " else ""}$content" }
    }

    fun process(
        jsonPath: File, outputDir: File?, data: ReportBookData, templateMap: Map<Int, File>
    ) {
        println("Processing: ${jsonPath.absolutePath}")

        val template = templateMap[data.year]!!
        val wordMlPackage = load(template)

        val variables = mapVariables(data)

        VariablePrepare.prepare(wordMlPackage)

        wordMlPackage.mainDocumentPart.variableReplace(variables)

        wordMlPackage.save(getOutputPath(jsonPath, outputDir, data))
    }

    private fun mapVariables(data: ReportBookData): Map<String, String> {
        val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        return mapOf(
            "number" to data.weekNumber.toString(),
            "week_start" to data.weekStart.format(dateFormatter),
            "week_end" to data.weekEnd.format(dateFormatter),
            "activity" to data.activity,
            "activity_hours" to data.activityHours.toString(),
            "teachings" to data.teachings,
            "teachings_hours" to data.teachingsHours.toString(),
            "school" to data.school,
            "school_hours" to data.schoolHours.toString()
        )
    }

    private fun getOutputPath(jsonPath: File, outputDir: File?, data: ReportBookData): File {
        val outputFormatter = DateTimeFormatter.ofPattern("yy-MM-dd")
        val weekStartValue = data.weekStart.format(outputFormatter)

        return File(outputDir ?: jsonPath.parentFile, "Nr${data.weekNumber}_$weekStartValue.docx")
    }

    fun copyWordFiles(inputDir: File, outputDir: File) {
        inputDir.walkTopDown()
            .filter { isWordFile(it) }
            .forEach {
                it.copyTo(File(outputDir, it.name))
            }
    }
}
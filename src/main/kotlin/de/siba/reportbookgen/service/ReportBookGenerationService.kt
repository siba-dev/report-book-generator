package de.siba.reportbookgen.service

import de.siba.reportbookgen.model.ReportBookWeekData
import de.siba.reportbookgen.model.ReportBookWeekJson
import de.siba.reportbookgen.model.SchoolSubjectsJson
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import org.docx4j.model.datastorage.migration.VariablePrepare
import org.docx4j.openpackaging.packages.WordprocessingMLPackage.load
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.*

class ReportBookGenerationService {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)!!

    companion object {
        val fileNameDateFormat = LocalDate.Formats.ISO
        val wordContentDateFormat = LocalDate.Format {
            day(); char('.'); monthNumber(); char('.'); year()
        }

        fun isJsonFile(path: Path): Boolean = path.extension.equals("json", ignoreCase = true)
        fun isWordFile(path: Path): Boolean = path.extension.equals("docx", ignoreCase = true)
    }

    fun copyWordFiles(inputDir: Path, outputDir: Path) {
        inputDir.walk()
            .filter { isWordFile(it) }
            .forEach {
                it.copyTo(outputDir.resolve(it.name))
            }
    }

    fun generate(
        inputDir: Path,
        outputDir: Path,
        yearMap: Map<Int, LocalDate>,
        templateMap: Map<Int, Path>
    ) {
        inputDir.walk()
            .filter { it.isRegularFile() && isJsonFile(it) }
            .forEach {
                process(it, outputDir, yearMap, templateMap)
            }
    }

    fun process(
        jsonPath: Path, outputDir: Path, yearMap: Map<Int, LocalDate>, templateMap: Map<Int, Path>
    ) {
        try {
            process(jsonPath, outputDir, loadData(jsonPath, yearMap), templateMap)
        } catch (e: Exception) {
            throw IllegalStateException("Error loading $jsonPath", e)
        }
    }

    private fun loadData(jsonPath: Path, yearMap: Map<Int, LocalDate>): ReportBookWeekData {
        val jsonContent = jsonPath.readText()
        val jsonModel = Json.decodeFromString<ReportBookWeekJson>(jsonContent)

        if (jsonModel.number == null) {
            val fileNameWithoutExt = jsonPath.nameWithoutExtension
            jsonModel.number = fileNameWithoutExt.toInt()
        }

        return mapData(jsonModel, yearMap)
    }

    private fun mapData(
        jsonModel: ReportBookWeekJson,
        yearMap: Map<Int, LocalDate>
    ): ReportBookWeekData {
        val weekNumber = jsonModel.number ?: throw IllegalStateException("Could not determine week number!")

        val startDate = yearMap[1] ?: throw IllegalArgumentException("Missing start date of year 1!")

        // Calculate week start (Monday) and end (Friday) dates
        val weekStart = startDate.plus(weekNumber - 1L, DateTimeUnit.WEEK)
        val weekEnd = weekStart.plus(4, DateTimeUnit.DAY) // Friday of the same week

        val year = yearMap
            .filter { it.value < weekStart }
            .maxBy { it.value }
            .key

        return ReportBookWeekData(
            weekNumber,
            weekStart,
            weekEnd,
            year,
            jsonModel.activity.joinToString(", "),
            jsonModel.activity_hours,
            jsonModel.teachings.joinToString(", "),
            jsonModel.teachings_hours,
            formatSchoolSubjects(jsonModel.school),
            jsonModel.school_days * 8
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
        jsonPath: Path, outputDir: Path, data: ReportBookWeekData, templateMap: Map<Int, Path>
    ) {
        logger.info("Processing '${jsonPath.absolute()}'")

        val templatePath = templateMap[data.year]!!
        val wordMlPackage = load(templatePath.toFile())

        val variables = mapVariables(data)

        VariablePrepare.prepare(wordMlPackage)

        wordMlPackage.mainDocumentPart.variableReplace(variables)

        wordMlPackage.save(getOutputPath(outputDir, data).toFile())
    }

    private fun mapVariables(data: ReportBookWeekData): Map<String, String> {
        return mapOf(
            "number" to data.weekNumber.toString(),
            "week_start" to data.weekStart.format(wordContentDateFormat),
            "week_end" to data.weekEnd.format(wordContentDateFormat),
            "activity" to data.activity,
            "activity_hours" to data.activityHours.toString(),
            "teachings" to data.teachings,
            "teachings_hours" to data.teachingsHours.toString(),
            "school" to data.school,
            "school_hours" to data.schoolHours.toString()
        )
    }

    private fun getOutputPath(outputDir: Path, data: ReportBookWeekData): Path {
        val weekStartValue = data.weekStart.format(fileNameDateFormat)

        return outputDir.resolve("Nr${data.weekNumber}_$weekStartValue.docx")
    }
}
package de.siba.reportbookgen.service

import de.siba.reportbookgen.model.ReportBookWeekData
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import org.docx4j.model.datastorage.migration.VariablePrepare
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.openpackaging.packages.WordprocessingMLPackage.load
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.io.path.extension
import kotlin.io.path.name
import kotlin.io.path.walk

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

    fun generateWordFiles(
        dataMap: Map<Path, ReportBookWeekData>, templateMap: Map<Int, Path>, outputDir: Path
    ) {
        dataMap.toSortedMap()
            .forEach { (_, data) ->
                logger.info("Generating word for week ${data.weekNumber}")

                val templatePath = templateMap[data.year]
                    ?: throw IllegalArgumentException("Template for year ${data.year} not found.")
                val wordMlPackage = load(templatePath.toFile())!!

                replaceVariables(data, wordMlPackage)

                wordMlPackage.save(getOutputPath(outputDir, data).toFile())
            }
    }

    private fun replaceVariables(
        data: ReportBookWeekData,
        wordMlPackage: WordprocessingMLPackage
    ) {
        val variables = mapVariables(data)
        VariablePrepare.prepare(wordMlPackage)
        wordMlPackage.mainDocumentPart.variableReplace(variables)
    }

    internal fun mapVariables(data: ReportBookWeekData): Map<String, String> {
        return mapOf(
            "number" to data.weekNumber.toString(),
            "year" to data.year.toString(),
            "week_start" to data.weekStart.format(wordContentDateFormat),
            "week_end" to data.weekEnd.format(wordContentDateFormat),
            "activity" to data.activity.joinToString(", "),
            "activity_hours" to data.activityHours.toString(),
            "teachings" to data.teachings.joinToString(", "),
            "teachings_hours" to data.teachingsHours.toString(),
            "school" to formatSchoolSubjects(data.school),
            "school_hours" to data.schoolHours.toString()
        )
    }

    private fun formatSchoolSubjects(school: Map<String, String>): String {
        return school.filter { it.value.isNotBlank() }
            .map { it.key + if (it.key.isBlank()) "" else ": " + it.value }
            .joinToString("; ")
    }

    private fun getOutputPath(outputDir: Path, data: ReportBookWeekData): Path {
        val weekStartValue = data.weekStart.format(fileNameDateFormat)

        return outputDir.resolve("Nr${data.weekNumber}_$weekStartValue.docx")
    }
}
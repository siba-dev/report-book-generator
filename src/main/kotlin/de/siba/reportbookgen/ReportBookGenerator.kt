package de.siba.reportbookgen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import de.siba.reportbookgen.service.ReportBookDataService
import de.siba.reportbookgen.service.ReportBookGenerationService
import de.siba.reportbookgen.service.ReportBookValidationService
import kotlinx.datetime.LocalDate
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    ReportBookGenerator().main(args)
}

class ReportBookGenerator : CliktCommand() {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)!!

    val inputDir by option(names = arrayOf("-i", "--inDirectory"), help = "Directory to get JSON files from.").path(
        mustExist = true, canBeFile = false, mustBeReadable = true
    )
        .required()
    val outputDir by option(
        names = arrayOf("-o", "--outDirectory"), help = "Directory to write generated word files to."
    ).path( //
        canBeFile = false, mustBeWritable = true
    )
        .required()

    val yearMap by option(
        names = arrayOf("-y", "--year"), help = "Apprenticeship year starts. Example: -y 1=2023-08-29"
    ).associate(transform = {
        Pair(
            it.first.toInt(), LocalDate.parse(it.second)
        )
    })
    val templateMap by option(
        names = arrayOf("-t", "--template"),
        help = "Defines which template to use for each year. This feature is available since some templates have a fixed year choice that can not be templated. Example: -t \"1= ~/template1.docx\""
    ).associate(transform = {
        Pair(
            it.first.toInt(), Path.of(it.second)
        )
    })

    val copyWordFiles by option(
        names = arrayOf("--copyWordFiles"), help = "Copy word files from input to output directory."
    ).flag(default = false)

    val maxHours by option(
        names = arrayOf("--maxHours"), help = "Specifies a limit that weekly hours can not exceed."
    ).int()

    override fun run() {
        logger.info("Cleaning output directory '$outputDir'")
        Files.list(outputDir)
            .forEach { Files.delete(it) }

        // Data
        val dataService = ReportBookDataService()
        val dataMap = dataService.loadAllWeeklyData(inputDir, yearMap)

        // Pre-Processing validation
        val validationService = ReportBookValidationService()
        if (maxHours != null) {
            validationService.validateMaxHours(dataMap, maxHours!!)
        }

        // Generation
        val generationService = ReportBookGenerationService()
        if (copyWordFiles) {
            generationService.copyWordFiles(inputDir, outputDir)
        }
        generationService.generateWordFiles(dataMap, templateMap, outputDir)

        // Post-Processing validation
        validationService.validateWeeks(outputDir)
    }
}
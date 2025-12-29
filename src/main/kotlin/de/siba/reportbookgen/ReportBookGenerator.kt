package de.siba.reportbookgen

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.path
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

    val inputDir by option(names = arrayOf("-i", "--inDirectory")).path(
        mustExist = true, canBeFile = false, mustBeReadable = true
    )
        .required()
    val outputDir by option(names = arrayOf("-o", "--outDirectory")).path( //
        canBeFile = false, mustBeWritable = true
    )
        .required()

    val yearMap by option(names = arrayOf("-y", "--year")).associate(transform = {
        Pair(
            it.first.toInt(), LocalDate.parse(it.second)
        )
    })
    val templateMap by option(names = arrayOf("-t", "--template")).associate(transform = {
        Pair(
            it.first.toInt(), Path.of(it.second)
        )
    })


    override fun run() {
        logger.info("Cleaning output directory '$outputDir'")
        Files.list(outputDir)
            .forEach { Files.delete(it) }

        // Generation
        val generationService = ReportBookGenerationService()

        generationService.copyWordFiles(inputDir, outputDir)
        generationService.generate(inputDir, outputDir, yearMap, templateMap)

        // Validation
        val validationService = ReportBookValidationService()
        validationService.validateWeeks(outputDir)
    }
}
package de.siba.reportbookgen

import de.siba.reportbookgen.service.ReportBookGenerationService
import de.siba.reportbookgen.service.ReportBookValidationService
import java.io.File

fun main(args: Array<String>) {
    if (args.size < 3) {
        throw IllegalArgumentException("Usage: java ${object {}.javaClass.enclosingClass.name} <inputDirectory> <outputDirectory> <templateYear1> <templateYear2>...")
    }

    val inputDir = File(args[0])
    val outputDir = (if (!args[1].isBlank()) File(args[1]) else null)!!

    val templateMap: Map<Int, File> = args.drop(2) // Skip the first argument
        .mapIndexed { index, arg -> index + 1 to File(arg) } // Indexes start at 1
        .toMap()

    outputDir.listFiles()
        ?.forEach { it.delete() }

    // Generation
    val generationService = ReportBookGenerationService()

    generationService.copyWordFiles(inputDir, outputDir)
    generationService.generateFromJsons(inputDir, outputDir, templateMap)

    // Validation
    val validationService = ReportBookValidationService()
    validationService.findMissingWeeks(outputDir)
}

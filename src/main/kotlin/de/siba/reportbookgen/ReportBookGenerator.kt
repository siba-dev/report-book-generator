package de.siba.reportbookgen

import de.siba.reportbookgen.service.MultiFileService
import de.siba.reportbookgen.service.MutliFileGenerationService
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

    // Copy and generation
    MultiFileService.copyWordFiles(inputDir, outputDir)
    MutliFileGenerationService.generateFromJsons(inputDir, outputDir, templateMap)

    // Validation
    MultiFileService.findMissingWeeks(outputDir)
}

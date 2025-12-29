package com.siba.reportbookgen

import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import java.io.File

fun main(args: Array<String>) {
    if (args.size != 1) {
        throw IllegalArgumentException("Usage: java com.siba.reportbookgen.ReportBookVariableAnalyzer <template.docx>")
    }

    val templateFile = File(args[0])
    if (!templateFile.isFile) {
        println("Template not found or not a file: $templateFile")
        return
    }

    ReportBookVariableAnalyzer.printVariablesInTemplate(templateFile)
}

object ReportBookVariableAnalyzer {
    fun printVariablesInTemplate(templateFile: File) {
        // Load fresh template for processing
        val wordMLPackage = WordprocessingMLPackage.load(templateFile)

        // Get the document content before replacement to see what variables exist
        val mainPart = wordMLPackage.mainDocumentPart
        val xmlBefore = mainPart.xml
        println("Looking for variables in template...")

        // Check for common variable patterns
        val dollarBracePattern = Regex("\\$\\{([^}]+)\\}")
        val dollarMatches = dollarBracePattern.findAll(xmlBefore)
        dollarMatches.iterator()
            .forEachRemaining { println(it.value) }
    }
}
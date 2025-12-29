package com.siba.reportbookgen.service

import java.io.File

object MultiFileService {
    fun copyWordFiles(inputDir: File, outputDir: File) {
        inputDir.walkTopDown()
            .filter { Utilities.isWord(it) }
            .forEach {
                it.copyTo(File(outputDir, it.name))
            }
    }

    fun findMissingWeeks(reportBookDirectory: File) {
        val foundNumbers = mutableListOf<Int>()

        reportBookDirectory.walkTopDown()
            .filter { Utilities.isWord(it) }
            .forEach {
                if (Utilities.isWord(it)) {
                    val nameAfterNr = it.name.drop(2)
                    val numberPart = nameAfterNr.takeWhile { ch -> ch != '_' }
                    val number = numberPart.toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid number format in file: ${it.name}")
                    foundNumbers.add(number)
                }
            }

        if (foundNumbers.isEmpty()) {
            throw IllegalArgumentException("No matching files found.")
        }

        foundNumbers.sort()

        // Check that numbers start at 1 and are sequential
        for (expected in 1..foundNumbers.last()) {
            if (expected != foundNumbers[expected - 1]) {
                throw IllegalStateException("Missing week $expected.")
            }
        }
    }
}


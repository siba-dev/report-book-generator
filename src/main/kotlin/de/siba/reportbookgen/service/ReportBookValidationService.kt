package de.siba.reportbookgen.service

import de.siba.reportbookgen.model.ReportBookWeekData
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.io.path.walk

class ReportBookValidationService {
    fun validateWeeks(reportBookDirectory: Path) {
        val foundNumbers = mutableListOf<Int>()

        reportBookDirectory.walk()
            .filter { ReportBookGenerationService.isWordFile(it) }
            .forEach {
                if (ReportBookGenerationService.isWordFile(it)) {
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

    fun validateMaxHours(dataMap: Map<Path, ReportBookWeekData>, maxHours: Int) {
        dataMap.forEach { (_, data) ->
            validateMaxHours(data, maxHours)
        }
    }

    fun validateMaxHours(data: ReportBookWeekData, maxHours: Int) {
        val totalHours = data.schoolHours + data.teachingsHours + data.activityHours
        if (totalHours > maxHours) {
            throw IllegalArgumentException("Hours of week ${data.weekNumber} add up to $totalHours, which is more than the allowed $maxHours hours.")
        }
    }
}
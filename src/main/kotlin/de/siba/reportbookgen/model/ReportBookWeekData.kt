package de.siba.reportbookgen.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

data class ReportBookWeekData(
    val weekNumber: Int,
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val year: Int,
    val activity: List<String>,
    val activityHours: Int,
    val teachings: List<String>,
    val teachingsHours: Int,
    val school: Map<String, String>,
    val schoolHours: Int
) {
    init {
        val totalHours = schoolHours + teachingsHours + activityHours
        if (totalHours > 40) {
            throw IllegalArgumentException("Hours add up to more then 40!")
        }
        if (totalHours < 32 && weekStart.month.number != 12) {
            throw IllegalArgumentException("Hours add up to less then 32!")
        }
        if (totalHours % 8 != 0) {
            throw IllegalArgumentException("Odd number of hours detected!")
        }
    }
}

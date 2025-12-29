package com.siba.reportbookgen.model

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

data class ReportBookData(
    val weekNumber: Int,
    val weekStart: LocalDate,
    val weekEnd: LocalDate,
    val year: Int,
    val activity: String,
    val activityHours: Int,
    val teachings: String,
    val teachingsHours: Int,
    val school: String,
    val schoolHours: Int
) {
    init {
        val totalHours = schoolHours + teachingsHours + activityHours
        if (totalHours > 40) {
            throw IllegalArgumentException("Hours add up to more then 40!")
        }
        if (totalHours < 32 && weekStart.monthValue != 12) {
            throw IllegalArgumentException("Hours add up to less then 32!")
        }
        if (totalHours % 8 != 0) {
            throw IllegalArgumentException("Odd number of hours detected!")
        }
    }
}

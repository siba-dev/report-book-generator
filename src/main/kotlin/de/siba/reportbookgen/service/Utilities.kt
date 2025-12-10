package de.siba.reportbookgen.service

import java.io.File

object Utilities {

    fun isWord(file: File): Boolean = file.name.startsWith("Nr") && file.extension.equals("docx", ignoreCase = true)

}
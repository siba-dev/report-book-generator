package com.siba.reportbookgen

import jakarta.xml.bind.JAXBElement
import org.docx4j.TextUtils
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.wml.*
import java.io.File
import java.io.StringWriter

fun main(args: Array<String>) {
    val wordMlPackage = WordprocessingMLPackage.load(File(args[0]))
    val jaxbElement = wordMlPackage.mainDocumentPart.jaxbElement.body.content.forEach { SchoolUpdater.search(it) }
}

/**
 * This may be used at a later point. Currently unused
 */
object SchoolUpdater {

    const val paraIdSchool = "04F283C3"

    fun search(e: Any?) {
        if (e == null) return

        if (e is JAXBElement<*>)
            search(e.value)
        if (e is Tbl)
            e.content.forEach { search(it) }

        if (e is Tr)
            e.content.forEach { search(it) }

        if (e is Tc)
            e.content.forEach { search(it) }
        if (e is P) {
            val writer = StringWriter()
            TextUtils.extractText(e, writer)
            val text = writer.toString()

            if (!text.isBlank()) println(text)
            if (text.contains("FORM"))
                e.content.forEach { search(it) }

            // TODO Move update here, insert Text object by subject
        }
        if (e is R)
            e.content.forEach { search(it) }
        if (e is Text && e.value != " FORMTEXT ")
            update(e)
    }

    fun update(e: Text) {
        val p = (e.parent as R).parent as P
        val paraId = p.paraId
        if (paraId == paraIdSchool) {
            e.value = "Siba"
        }
    }

}
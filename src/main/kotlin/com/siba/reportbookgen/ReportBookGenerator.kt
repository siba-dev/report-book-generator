package com.siba.reportbookgen

import jakarta.xml.bind.JAXBElement
import org.docx4j.TextUtils
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import org.docx4j.wml.*
import java.io.File
import java.io.StringWriter

const val textIdNumber = "6A2A2C99"
const val textIdActivities = "1C2E8B4C"

fun main() {
    val wordMLPackage = WordprocessingMLPackage.load(
        File("C:\\Users\\sim.baue\\Downloads\\playground.docx")
    )
    val elements = wordMLPackage.mainDocumentPart.jaxbElement.body.content
    elements.forEach { search(it) }

    wordMLPackage.save(File("C:\\Users\\sim.baue\\Downloads\\playground_updated.docx"))
}

fun search(e: Any?) {
    if(e == null) return

    if(e is JAXBElement<*>)
        search(e.value)
    if(e is Tbl)
        e.content.forEach { search(it) }

    if(e is Tr)
        e.content.forEach { search(it) }

    if(e is Tc)
        e.content.forEach { search(it) }
    if(e is P) {
        val writer = StringWriter()
        TextUtils.extractText(e, writer)
        val text = writer.toString()

        if(!text.isBlank()) println(text)
        if(text.contains("FORM"))
            e.content.forEach { search(it) }
    }
    if(e is R)
        e.content.forEach { search(it) }
    if(e is Text && e.value != " FORMTEXT ")
        update(e)
}

fun update(e: Text) {
    val p = (e.parent as R).parent as P
    val textId = p.textId
    if(textId == textIdNumber) {
        e.value = "1234"
    }
    if(textId == textIdActivities) {
        e.value = "Activities"
    }
}

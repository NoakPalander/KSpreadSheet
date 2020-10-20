package com.spreadsheet.example

import com.palander.spreadsheet.Core
import java.lang.RuntimeException
import java.net.URL

fun getResource(name: String): URL {
    return {}::class.java.getResource(name) ?: throw RuntimeException("Invalid resource '$name'!")
}


fun main() {
    val headerStyle: String = getResource("/header_style.json").readText()
    val textStyle: String = getResource("/text_style.json").readText()
    val credentials: String = getResource("/credentials.json").path

    Core.initialize("tokens", credentials, "spreadsheet id", "sheet id")
    Core.appendRow("sheet id", listOf("The", "column", "values", "to pass"))
    Core.formatCells(headerStyle, textStyle)
}

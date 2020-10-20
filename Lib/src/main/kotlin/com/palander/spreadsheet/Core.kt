package com.palander.spreadsheet

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.services.sheets.v4.model.*
import java.io.FileReader
import java.lang.RuntimeException

class Core {
    companion object {
        private const val appName = "com.palander.spreadsheet.Core"
        private var id: String? = null
        private var sheetService: Sheets? = null
        var headers: List<*>? = null

        private fun authorize(tokenLoc: String, credentials: String): Credential {
            val secrets = GoogleClientSecrets.load(JacksonFactory.getDefaultInstance(), FileReader(credentials))

            val scopes = mutableListOf(SheetsScopes.SPREADSHEETS)
            val flow = GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), secrets, scopes)
                .setDataStoreFactory(FileDataStoreFactory(java.io.File(tokenLoc)))
                .setAccessType("offline")
                .build()

            return AuthorizationCodeInstalledApp(flow, LocalServerReceiver()).authorize("user")
        }

        fun initialize(tokenLoc: String, credentials: String, spreadsheetId: String, sheetName: String) {
            // Initialize the sheet service
            sheetService = Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
                    authorize(tokenLoc, credentials))
                    .setApplicationName(appName)
                    .build()

            id = spreadsheetId

            // Read the headers
            headers = sheetService!!.spreadsheets().values().get(id, sheetName).execute().getValues()[0]
        }

        fun appendRow(sheetName: String, data: List<String>): AppendValuesResponse {
            val appendBody = ValueRange().setValues(listOf(data))

            return sheetService?.spreadsheets()?.values()?.append(id, sheetName, appendBody)
                    ?.setValueInputOption("USER_ENTERED")
                    ?.setInsertDataOption("INSERT_ROWS")
                    ?.setIncludeValuesInResponse(true)
                    ?.execute() ?: throw RuntimeException("Sheet service isn't initialized. Run Core.initialize first!")
        }

        fun formatCells(vararg formats: String): BatchUpdateSpreadsheetResponse {
            val requests = Array(formats.size){ ObjectMapper().readValue(formats[it], Map::class.java )}.toMutableList()
            val body = BatchUpdateSpreadsheetRequest().set("requests", requests)
            
            return sheetService?.spreadsheets()
                    ?.batchUpdate(id, body)
                    ?.execute() ?: throw RuntimeException("Sheet service isn't initialized. Run Core.initialize first!")
        }
    }
}
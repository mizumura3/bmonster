package bmonster.client

import bmonster.SpreadSheetRequest
import bmonster.SpreadSheetResponse
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * google spread sheet を操作するクライアント
 */
class SpreadSheetClient {

    val jsonFactory = JacksonFactory.getDefaultInstance()
    val TOKEN_DIRECTORY_PATH = "tokens"

    val SCOPES = listOf(SheetsScopes.SPREADSHEETS)

    /**
     * spread sheet にアクセスする
     */
    fun getSheetData(request: SpreadSheetRequest): SpreadSheetResponse {
        val applicationName = "bmonster spreadsheet"
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()

        val service = Sheets.Builder(
            httpTransport,
            jsonFactory,
            getCredential(httpTransport)).setApplicationName(applicationName)
            .build()

        while (true) {
            service.spreadsheets().get(request.spreadSheetId)

            val response = service
                .spreadsheets()
                .values()
                .get(request.spreadSheetId, request.range)
                .execute()

            val values = response.getValues()

            // TODO まだ予約、移動していないサンドバッグを返却する
            if (values == null || values.isEmpty()) {
                println("no data found.")
            } else {
                println("url, sandbag")
                for (row in values) {

                    // url がなければ無限ループ継続 1分待機
                    if (row[0] == "") {
                        println("wait 1 minutes")
                        TimeUnit.MINUTES.sleep(1)
                        break
                    }

                    return SpreadSheetResponse(
                        url = row[0] as String,
                        sandbag = row[1] as String
                    )
                }
            }
        }
    }

    /**
     * 認証情報を取得する
     */
    fun getCredential(httpTransport: NetHttpTransport): Credential {
        val credential = File(System.getenv("BMONSTER_CREDENTIAL")).inputStream()
        val clientSecret = GoogleClientSecrets.load(jsonFactory, InputStreamReader(credential))

        val flow = GoogleAuthorizationCodeFlow.Builder(
            httpTransport, jsonFactory, clientSecret, SCOPES
        ).setDataStoreFactory(
            FileDataStoreFactory(
                File(TOKEN_DIRECTORY_PATH)
            )
        ).setAccessType("offline")
            .build()

        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

}
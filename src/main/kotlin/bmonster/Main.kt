package bmonster

import bmonster.client.BmonsterClient
import bmonster.client.SpreadSheetClient
import bmonster.service.MoveService
import bmonster.service.ReserveService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import java.io.File
import java.util.*

/**
 * bmonster の予約 cli
 */
class Reserve : CliktCommand() {
    val lessonUrl: String by option(help = "input lesson url").prompt("lesson url")

    // nullable にして null ならプロパティのメールアドレスとパスワードを使う
    val mail: String? by option(help = "mail address")
    val password: String? by option(help = "password")
    val bags: String? by option(help = "punchbag num, multiply with comma 1,2,3")
    val interval: String? by option(help = "refresh interval, default 5sec")

    val properties = Properties().apply { File("./src/main/resources/config.properties").inputStream().use(this::load) }

    override fun run() {

        // 引数に指定されていない場合は config.properties の値を取得する
        val mail = mail ?: properties["mail_address"].toString()
        val password = password ?: properties["password"].toString()

        val studioCode = lessonUrl.substring(lessonUrl.indexOf("studio_code=")).split("=")[1]
        val lessonId = lessonUrl.substring(lessonUrl.indexOf("lesson_id=")).split("&")[0].split("=")[1]
        val interval = interval

        val data = ReserveData.create(
            mail = mail,
            password = password,
            studioCode = Studio.from(studioCode),
            lessonId = lessonId,
            lessonUrl = lessonUrl,
            interval = toInterval(interval),
            bags = bags
        )

        echo("lesson $data")

        // 予約実行
        val r = ReserveService(BmonsterClient())
        r.reserve(data)
    }
}

/**
 * サンドバッグ移動 cli
 */
class Move : CliktCommand() {

    val lessonUrl: String by option(help = "input lesson url").prompt("lesson url")

    // nullable にして null ならプロパティのメールアドレスとパスワードを使う
    val mail: String? by option(help = "mail address")
    val password: String? by option(help = "password")
    val bags: String by option(help = "punchbag num, multiply with comma 1,2,3").prompt("bag numbers 1,2,3...")
    val interval: String? by option(help = "refresh interval, default 5sec")

    val properties = Properties().apply { File("./src/main/resources/config.properties").inputStream().use(this::load) }

    override fun run() {

        // 引数に指定されていない場合は config.properties の値を取得する
        val mail = mail ?: properties["mail_address"].toString()
        val password = password ?: properties["password"].toString()

        val reservationId = lessonUrl.substring(lessonUrl.indexOf("reservation_id=")).split("&")[0].split("=")[1]
        val lessonId = lessonUrl.substring(lessonUrl.indexOf("lesson_id=")).split("=")[1]
        val interval = interval

        println("reservation id $reservationId")
        println("lesson id $lessonId")

        val data = MoveData.create(
            mail = mail,
            password = password,
            reservationId = reservationId,
            lessonId = lessonId,
            lessonUrl = lessonUrl,
            interval = toInterval(interval),
            bags = bags
        )

        val m = MoveService(BmonsterClient())
        m.move(data)
        echo("move")
    }
}

/**
 * スプレッドシートで移動する cli
 */
class WebMove : CliktCommand() {
    // nullable にして null ならプロパティのメールアドレスとパスワードを使う
    val mail: String? by option(help = "mail address")
    val password: String? by option(help = "password")
    val sheetId: String by option(help = "google spread sheet id").prompt("enter spread sheet id")

    val properties = Properties().apply { File("./src/main/resources/config.properties").inputStream().use(this::load) }

    override fun run() {
        // 引数に指定されていない場合は config.properties の値を取得する
        val mail = mail ?: properties["mail_address"].toString()
        val password = password ?: properties["password"].toString()
        val spreadSheetId = sheetId
        val interval = "5"

        val client = SpreadSheetClient()
        val request = SpreadSheetRequest(
            spreadSheetId = spreadSheetId,
            range = "move!A2:B"
        )

        val response = client.getSheetData(request)

        val data = MoveData.create(
            mail = mail,
            password = password,
            reservationId = response.url.toReservationId(),
            lessonId = response.url.toLessonId(),
            lessonUrl = response.url,
            interval = toInterval(interval),
            bags = response.sandbag
        )

        println(data)

        val m = MoveService(BmonsterClient())
        m.move(data)
        echo("move")
    }
}


private fun toInterval(interval: String?): Long {
     return if (interval == null) {
         DEFAULT_REFRESH_INTERVAL
     } else {
         interval.toLong() * 1000
     }
}

class Command : CliktCommand() {
    override fun run() = Unit
}

fun main(args: Array<String>) = Command()
    .subcommands(Reserve(), Move(), WebMove())
    .main(args)

package bmonster.client

import bmonster.MoveData
import bmonster.MovePunchbag
import bmonster.ReserveData
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDateTime

/**
 * bmonster の api 呼び出しを行うクラス
 */
class BmonsterClient {

    companion object {
        // User-Agent
        const val UA =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36"

        /**
         * サンドバッグ番号で有効な物を取得する
         */
        fun punchBags(doc: Document): Set<Int> =
            doc.select("input[id^=bag]")
                .filter { it.attributes().size() != 3 }
                .map { it.attributes().get("id").substringAfter("bag").toInt() }
                .toSet()
    }

    /**
     * 予約 api を呼ぶ
     */
    fun reserve(authToken: String, onetimeToken: String, punchbagNo: Int, data: ReserveData) {
        val reserve = "https://www.b-monster.jp/api/reservation/${data.lessonId}/reserve"
        val conn = postConnectionBase(
            url = reserve,
            authToken = authToken,
            onetimeToken = onetimeToken,
            lessonUrl = data.lessonUrl
        )

        val res = conn.data("no_and_members", "[{\"no\":\"${punchbagNo}\"}]").execute()
        println("${LocalDateTime.now()}: ${res.body()} sandbag: $punchbagNo")
    }

    /**
     * サンドバッグを移動する
     */
    fun move(authToken: String, movePunchBag: MovePunchbag, data: MoveData) {
        val move = "https://www.b-monster.jp/api/reservation/${data.lessonId}/move"

        val conn = putConnectionBase(
            url = move,
            authToken = authToken,
            onetimeToken = movePunchBag.onetimeToken,
            lessonUrl = data.lessonUrl
        )

        val res = conn
            .data("no", movePunchBag.currentNo)
            .data("next_no", movePunchBag.nextNo)
            .execute()

        println("${LocalDateTime.now()}: ${res.body()} move to: $movePunchBag")
    }

    /**
     * PUT 実行用の Connection を返却する
     */
    private fun putConnectionBase(
        url: String,
        authToken: String,
        onetimeToken: String,
        lessonUrl: String
    ): Connection {

        val top = getTopPage()
        return Jsoup.connect(url)
            .userAgent(UA)
            .cookies(top.cookies())
            .headers(top.headers())
            .headers(headers())
            .ignoreHttpErrors(true)
            .ignoreContentType(true)
            .referrer(lessonUrl)
            .cookie("auth_token_web", authToken)
            .data("onetime_token", onetimeToken)
            .method(Connection.Method.PUT)
    }

    /**
     * POST 用の header を設定する
     */
    private fun postConnectionBase(
        url: String,
        authToken: String,
        onetimeToken: String,
        lessonUrl: String
    ): Connection {

        val top = getTopPage()
        return Jsoup.connect(url)
            .userAgent(UA)
            .cookies(top.cookies())
            .headers(top.headers())
            .headers(headers())
            .ignoreHttpErrors(true)
            .ignoreContentType(true)
            .referrer(lessonUrl)
            .cookie("auth_token_web", authToken)
            .data("onetime_token", onetimeToken)
            .method(Connection.Method.POST)
    }

    /**
     * 認証のトークンを取得する
     */
    fun getAuthToken(mail: String, password: String): String {
        val top = getTopPage()

        // auth_token_web 取得
        val doc = Jsoup.connect("https://www.b-monster.jp/api/member/signin")
            .userAgent(UA)
            .cookies(top.cookies())
            .headers(top.headers())
            .headers(headers())
            .ignoreHttpErrors(true)
            .ignoreContentType(true)
            .referrer("https://www.b-monster.jp/")
            .data("mail_address", mail)
            .data("password", password)
            .method(Connection.Method.POST)
            .execute()

        return doc.cookie("auth_token_web")
    }

    /**
     * ワンタイムトークンを取得する
     */
    fun getOnetimeToken(authToken: String, data: ReserveData): String {

        val memberPage = getMemberPage(authToken)

        val confirm =
            "https://www.b-monster.jp/reserve/confirm?lesson_id=${data.lessonId}&studio_code=${data.studioCode.code}&punchbag=1"
        println("confirm page $confirm")

        val response = Jsoup.connect(confirm)
            .cookies(memberPage.cookies())
            .cookie("auth_token_web", authToken)
            .header("Referer", data.lessonUrl)
            .headers(memberPage.headers())
            .method(Connection.Method.GET)
            .execute()

        // ワンタイムトークン取得
        val onetimeToken = response
            .parse()
            .select("input[name=one_time_token]")[0]
            .attributes().get("value")

        println("onetime_token $onetimeToken")

        return onetimeToken
    }

    /**
     * トップページを取得する
     */
    private fun getTopPage(): Connection.Response {
        return Jsoup
            .connect("https://www.b-monster.jp/")
            .method(Connection.Method.GET).execute()
    }

    /**
     * メンバーページに飛ぶ
     */
    private fun getMemberPage(authToken: String): Connection.Response {
        return Jsoup
            .connect("https://www.b-monster.jp/mypage/")
            .cookie("auth_token_web", authToken)
            .method(Connection.Method.GET).execute()
    }

    /**
     * POST 用の http request header を返却する
     */
    private fun headers(): Map<String, String> {
        return mutableMapOf(
            "Content-type" to "application/x-www-form-urlencoded; charset=UTF-8",
            "Accept" to "*/*",
            "Origin" to "https://www.b-monster.jp",
            "Referer" to "https://www.b-monster.jp/",
            "X-Requested-With" to "XMLHttpRequest"
        )
    }

}

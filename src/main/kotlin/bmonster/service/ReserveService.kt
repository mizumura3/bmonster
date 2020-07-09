package bmonster.service

import bmonster.*
import bmonster.client.BmonsterClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDateTime

/**
 * 予約
 */
class ReserveService(
    private val client: BmonsterClient
) {

    /**
     * 予約実行
     */
    fun reserve(data: ReserveData) {
        val authToken = client.getAuthToken(data.mail, data.password)
        println("auth token $authToken")

        val punchBagNo = refresh(authToken, data)
        println("punchBagNo = $punchBagNo")

        client.reserve(
            authToken = authToken,
            onetimeToken = client.getOnetimeToken(authToken, data),
            punchbagNo = punchBagNo,
            data = data
        )
    }

    /**
     * サンドバッグの空きがでるまで更新し続ける
     */
    fun refresh(authToken: String, data: ReserveData): Int {

        // 予約画面の url
        val reserve =
            "https://www.b-monster.jp/reserve/punchbag?lesson_id=${data.lessonId}&studio_code=${data.studioCode.code}"

        var pastDateTime = LocalDateTime.now()
        var token = authToken

        while (true) {

            // 3時間ごとに auth token を更新する
            val now = LocalDateTime.now()
            if (pastDateTime.plusHours(3) < now) {
                pastDateTime = LocalDateTime.now()
                println("auth token 更新")
                token = client.getAuthToken(data.mail, data.password)
            }

            val doc = Jsoup
                .connect(reserve)
                .cookie("auth_token_web", token)
                .get()

            // 満員 または チケットが必要な場合は更新を継続する
            if (isFull(doc) || needTicket(doc)) {
                println("${LocalDateTime.now()} 満員かチケットが必要なので更新継続")
                Thread.sleep(data.interval) // デフォルト5秒停止
                continue
            }

            // バッグの番号を取得する
            // 指定のバッグが取得できなかった場合はランダムのバッグを取得する
            return getPunchBagNo(doc, data)
        }
    }

    /**
     * 満員かどうか判定する
     */
    private fun isFull(doc: Document): Boolean {
        val full = doc.selectFirst("h2[class=smooth-text]")

        if (full.text().contains("満員")) {
            println("満員")
        } else {
            println("満員ではない")
        }

        return full.text().contains("満員")
    }

    /**
     * チケットが必要かどうか判定する（予約の枠が余っているか判定）
     */
    private fun needTicket(doc: Document): Boolean {

        // 警告メッセージが表示されない場合は予約できるので false を返す
        val ticket = doc.selectFirst("div.caution-reserve-punchbag") ?: return false

        // メッセージを取得する
        val ticketText = ticket.childNode(0).outerHtml()

        if (ticketText != null) {
            if (ticketText.contains("チケット") || ticketText.contains("一度にご予約")) {
                println("チケットが必要 or 予約上限")
                return true
            }
        }

        return false
    }

    /**
     * バッグの番号を取得する
     */
    private fun getPunchBagNo(doc: Document, data: ReserveData): Int {
        // サンドバッグ取得
        // disabled のチェックボックをを除外する(attribute が3のものは disabled で予約ずみ)
        val punchbags = BmonsterClient.punchBags(doc)

        println("available punchbags: $punchbags")

        for (bag in data.punchBags) {
            if (punchbags.contains(bag)) {
                return bag
            }
        }

        return punchbags.random()
    }
}

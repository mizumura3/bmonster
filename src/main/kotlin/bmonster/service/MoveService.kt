package bmonster.service

import bmonster.client.BmonsterClient
import bmonster.MoveData
import bmonster.MovePunchbag
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDateTime

/**
 * パンチバッグ移動のサービスクラス
 */
class MoveService(
    private val client: BmonsterClient
) {

    fun move(data: MoveData) {
        val authToken = client.getAuthToken(data.mail, data.password)
        println("auth token $authToken")

        val movePunchBag = refresh(authToken, data)
        client.move(
            authToken = authToken,
            movePunchBag = movePunchBag,
            data = data
        )
    }

    fun refresh(authToken: String, data: MoveData): MovePunchbag {

        val move = "https://www.b-monster.jp/reserve/move?reservation_id=${data.reservationId}&lesson_id=${data.lessonId}"

        var pastDateTime = LocalDateTime.now()
        var token = authToken

        // サンドバッグ番号が取得できるまで実行する更新し続ける
        while (true) {

            // 3時間ごとに auth token を更新する
            val now = LocalDateTime.now()
            if (pastDateTime.plusHours(3) < now) {
                pastDateTime = LocalDateTime.now()
                println("auth token 更新")
                token = client.getAuthToken(data.mail, data.password)
            }

            val doc = Jsoup
                .connect(move)
                .cookie("auth_token_web", token)
                .get()

            val punchBagNo = getPunchBagNo(doc, data)

            if (punchBagNo == null) {
                println("${LocalDateTime.now()}: 指定のサンドバッグ番号は予約済みのためリトライする ${data.punchBags}")
                Thread.sleep(data.interval) // 指定した秒数停止する
                continue
            }

            // 現在のサンドバッグ番号を取得する
            val currentPunchBagNo = doc
                .selectFirst("input[type=hidden][name=no]")
                .attributes()
                .get("value")

            val onetimeToken = doc.
                selectFirst("input[type=hidden][name=one-time-token]")
                .attributes()
                .get("value")

            // 現在のサンドバッグ番号と次に予約するサンドバッグ番号を返却する
            return MovePunchbag(
                currentNo = currentPunchBagNo,
                nextNo = punchBagNo.toString(),
                onetimeToken = onetimeToken
            )
        }
    }

    /**
     * 指定したサンドバッグと一致する場合はサンドバッグ番号を返却する。
     * 指定したサンドバッグ番号が取得できなかった場合は null を返却する。
     *
     */
    private fun getPunchBagNo(doc: Document, data: MoveData): Int? {
        val punchBags = BmonsterClient.punchBags(doc)
        println("available punchbags: $punchBags")

        for (bag in data.punchBags) {
            if (punchBags.contains(bag)) {
                return bag
            }
        }

        return null
    }

}

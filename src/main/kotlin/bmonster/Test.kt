package bmonster

import bmonster.client.BmonsterClient
import org.jsoup.Jsoup

fun main() {

    val authToken = BmonsterClient().getAuthToken(
        "",
        ""
    )

    val elements = Jsoup
        .connect("https://www.b-monster.jp/reserve/punchbag?lesson_id=114753")
        .cookie("auth_token_web", authToken)
        .get()
        .selectFirst("h2[class=smooth-text]")

    println(elements.childNode(0).outerHtml())
}

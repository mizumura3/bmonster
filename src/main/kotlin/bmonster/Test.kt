package bmonster

import bmonster.client.BmonsterClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

fun main() {

    val authToken = BmonsterClient().getAuthToken(
        "",
        ""
    )

    val elements = Jsoup
        .connect("https://www.b-monster.jp/reserve/punchbag?lesson_id=116042&studio_code=0004")
        .cookie("auth_token_web", authToken)
        .get()
        .select("input[id^=bag][disabled!=disabled]")

    val list = mutableListOf<Element>()

    val hoge = elements
        .filter { it.parent().select(".hidden").isEmpty() }
        .map { it.attributes().get("id").substringAfter("bag").toInt() }


    for (e in elements) {
        if (e.parent().select(".hidden").isEmpty()) {
            list.add(e)
        }
    }

    println(elements)
}

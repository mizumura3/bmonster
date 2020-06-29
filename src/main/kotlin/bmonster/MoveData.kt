package bmonster

/**
 * 移動データクラス
 */
data class MoveData(
    val mail: String,
    val password: String,
    val reservationId: String,
    val lessonId: String,
    val lessonUrl: String,
    val interval: Long,
    val punchBags: List<Int>
) {

    companion object {

        fun create(
            mail: String,
            password: String,
            reservationId: String,
            lessonId: String,
            lessonUrl: String,
            interval: Long,
            bags: String
        ): MoveData =
            MoveData(
                mail = mail,
                password = password,
                reservationId = reservationId,
                lessonId = lessonId,
                lessonUrl = lessonUrl,
                interval = interval,
                punchBags = bags.split(",").map { it.toInt() }
            )

    }
}

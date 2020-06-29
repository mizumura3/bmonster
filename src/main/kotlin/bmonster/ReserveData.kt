package bmonster

/**
 * 予約データクラス
 */
data class ReserveData(
    val mail: String,
    val password: String,
    val studioCode: Studio,
    val lessonId: String,
    val lessonUrl: String,
    val interval: Long,
    val punchBags: List<Int>
) {

    companion object {

        /**
         * インスタンスを生成する
         */
        fun create(
            mail: String,
            password: String,
            studioCode: Studio,
            lessonId: String,
            lessonUrl: String,
            interval: Long,
            bags: String?
        ): ReserveData =
            ReserveData(
                mail = mail,
                password = password,
                studioCode = studioCode,
                lessonId = lessonId,
                lessonUrl = lessonUrl,
                interval = interval,
                punchBags = bags(bags, studioCode)
            )

        /**
         * パンチバッグが指定されている場合は指定の番号を設定する。
         * 指定されていない場合は定義済みの番号を設定する。
         */
        private fun bags(bags: String?, studioCode: Studio): List<Int> {

            if (bags != null) {
                val intBags = bags.split(",").map { it.toInt() }
                println("bags $intBags")
                return intBags
            }

            println("set studio bags")

            return when (studioCode) {
                Studio.GINZA -> {
                    GINZA_PUNCHBAGS
                }

                Studio.AOYAMA -> {
                    AOYAMA_PUNCHBAGS
                }

                Studio.EBISU -> {
                    EBISU_PUNCHBAGS
                }

                Studio.SHINJUKU -> {
                    SHINJUKU_PUNCHBAGS
                }

                Studio.IKEBUKURO -> {
                    IKEBUKURO_PUNCHBAGS
                }
            }
        }

    }
}

package bmonster

/**
 * スタジオコード
 */
enum class Studio(val code: String) {
    GINZA("0001"),
    AOYAMA("0002"),
    EBISU("0003"),
    SHINJUKU("0004"),
    IKEBUKURO("0006"),
    ;

    companion object {
        fun from(code: String): Studio {
            return values().find { it.code == code } ?: throw IllegalArgumentException("invalid code. code = $code")
        }
    }
}

/**
 * 銀座のバッグ
 */
val GINZA_PUNCHBAGS = listOf(
    1
)

/**
 * エビスのバッグ
 */
val EBISU_PUNCHBAGS = listOf(
    1
)

/**
 * 新宿のバッグ
 */
val SHINJUKU_PUNCHBAGS = listOf(
    67, 68, 69, 70, 71, 55, 56, 57, 58, 59,
    79, 80, 81, 82, 83, 84, 85,
    73, 74, 75, 76, 77, 78,
    33, 34, 35, 36, 37, 38, 39,
    40, 41, 42, 43, 44, 45,
    17, 18, 19, 20, 21, 22, 23, 24,
    26, 27, 28, 29, 30, 31, 32,
    1, 2, 3, 4, 5, 6, 7,
    10, 11, 12, 15, 16,
    86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99
)

/**
 * 青山のバッグ
 */
val AOYAMA_PUNCHBAGS = listOf(
    1
)

/**
 * 池袋のバッグ
 */
val IKEBUKURO_PUNCHBAGS = listOf(
    1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
    11, 12, 15, 16, 17, 23, 24, 25,
    26, 30, 31, 37, 38, 39, 40, 41,
    44, 45, 46, 47, 48, 49, 50, 51,
    52, 53, 54, 55, 56
)

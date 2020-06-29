package bmonster

data class SpreadSheetRequest(
    /** スプレッドシートの ID */
    val spreadSheetId: String,
    val range: String
)
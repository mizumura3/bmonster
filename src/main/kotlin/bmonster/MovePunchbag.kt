package bmonster

/**
 * 現在のサンドバッグ番号と移動するサンドバッグ番号を保持する
 */
data class MovePunchbag(
    val currentNo: String,
    val nextNo: String,
    val onetimeToken: String
)

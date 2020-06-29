package bmonster

fun String.toReservationId(): String = this.substring(this.indexOf("reservation_id=")).split("&")[0].split("=")[1]
fun String.toLessonId(): String = this.substring(this.indexOf("lesson_id=")).split("=")[1]
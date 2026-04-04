package com.giwon.blog.common.exception

enum class ErrorCode(val status: Int, val message: String) {
    ARTICLE_NOT_FOUND(404, "게시글을 찾을 수 없습니다"),
    ALREADY_PUBLISHED(400, "이미 발행된 글입니다"),
    INVALID_SCHEDULE_TIME(400, "예약 시간은 현재보다 미래여야 합니다"),
    INVALID_REQUEST(400, "잘못된 요청입니다"),
    INTERNAL_ERROR(500, "서버 내부 오류가 발생했습니다"),
}

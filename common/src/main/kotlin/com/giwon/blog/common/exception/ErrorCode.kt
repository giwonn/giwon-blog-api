package com.giwon.blog.common.exception

enum class ErrorCode(val status: Int, val message: String) {
    ARTICLE_NOT_FOUND(404, "게시글을 찾을 수 없습니다"),
    INVALID_REQUEST(400, "잘못된 요청입니다"),
    INTERNAL_ERROR(500, "��버 내부 오류가 발생했습니다"),
}

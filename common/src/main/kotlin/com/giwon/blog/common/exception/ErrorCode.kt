package com.giwon.blog.common.exception

enum class ErrorCode(val status: Int, val message: String) {
    ARTICLE_NOT_FOUND(404, "게시글을 찾을 수 없습니다"),
    ARTICLE_PASSWORD_REQUIRED(403, "비밀번호가 필요한 게시글입니다"),
    ARTICLE_PASSWORD_INCORRECT(403, "비밀번호가 올바르지 않습니다"),
    INVALID_REQUEST(400, "잘못된 요청입니다"),
    INTERNAL_ERROR(500, "서버 내부 오류가 발생했습니다"),
}

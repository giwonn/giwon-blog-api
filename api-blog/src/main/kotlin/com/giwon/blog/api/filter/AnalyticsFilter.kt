package com.giwon.blog.api.filter

import com.giwon.blog.core.analytics.application.AnalyticsCollectionService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Component
class AnalyticsFilter(
    private val analyticsCollectionService: AnalyticsCollectionService,
) : OncePerRequestFilter() {

    companion object {
        private const val SESSION_COOKIE_NAME = "blog-session"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        filterChain.doFilter(request, response)

        val isArticlePath = request.requestURI.matches(Regex("^/articles/\\d+$"))
        if (request.method == "GET" && response.status == 200 && isArticlePath) {
            val sessionId = resolveSessionId(request, response)
            analyticsCollectionService.recordPageView(
                path = request.requestURI,
                ipAddress = request.getHeader("X-Forwarded-For") ?: request.remoteAddr,
                userAgent = request.getHeader("User-Agent"),
                referrer = request.getHeader("Referer"),
                sessionId = sessionId,
            )
        }
    }

    private fun resolveSessionId(request: HttpServletRequest, response: HttpServletResponse): String {
        val existing = request.cookies?.find { it.name == SESSION_COOKIE_NAME }?.value
        if (existing != null) return existing

        val newSessionId = UUID.randomUUID().toString()
        val cookie = Cookie(SESSION_COOKIE_NAME, newSessionId).apply {
            path = "/"
            maxAge = secondsUntilMidnight()
            isHttpOnly = true
        }
        response.addCookie(cookie)
        return newSessionId
    }

    private fun secondsUntilMidnight(): Int {
        val now = LocalDateTime.now()
        val midnight = now.toLocalDate().plusDays(1).atStartOfDay()
        return Duration.between(now, midnight).seconds.toInt()
    }
}

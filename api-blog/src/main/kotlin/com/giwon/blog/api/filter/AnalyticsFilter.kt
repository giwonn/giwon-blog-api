package com.giwon.blog.api.filter

import com.giwon.blog.core.analytics.application.AnalyticsCollectionService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AnalyticsFilter(
    private val analyticsCollectionService: AnalyticsCollectionService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        filterChain.doFilter(request, response)

        val isExcluded = request.requestURI.startsWith("/sidebar/") || request.requestURI == "/health"
        if (request.method == "GET" && response.status == 200 && !isExcluded) {
            val sessionId = request.getHeader("X-Session-Id")
            analyticsCollectionService.recordPageView(
                path = request.requestURI,
                ipAddress = request.getHeader("X-Forwarded-For") ?: request.remoteAddr,
                userAgent = request.getHeader("User-Agent"),
                referrer = request.getHeader("Referer"),
                sessionId = sessionId,
            )
        }
    }
}

package com.giwon.blog.core.analytics.application

import com.giwon.blog.core.analytics.domain.AnalyticsWriter
import com.giwon.blog.core.analytics.domain.PageView
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AnalyticsCollectionService(
    private val analyticsWriter: AnalyticsWriter,
) {

    @Async
    @Transactional
    fun recordPageView(
        path: String,
        ipAddress: String,
        userAgent: String?,
        referrer: String?,
        sessionId: String?,
    ) {
        val pageView = PageView(
            path = path,
            ipAddress = ipAddress,
            userAgent = userAgent,
            referrer = referrer,
            sessionId = sessionId,
        )
        analyticsWriter.savePageView(pageView)

        if (sessionId != null) {
            analyticsWriter.upsertSession(sessionId, ipAddress, userAgent)
        }
    }
}

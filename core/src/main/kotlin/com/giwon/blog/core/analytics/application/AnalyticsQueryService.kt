package com.giwon.blog.core.analytics.application

import com.giwon.blog.core.analytics.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class AnalyticsQueryService(
    private val analyticsReader: AnalyticsReader,
) {

    fun getOverview(from: LocalDateTime, to: LocalDateTime): AnalyticsOverview {
        val totalPageViews = analyticsReader.findTopPages(from, to).sumOf { it.viewCount }
        val uniqueVisitors = analyticsReader.countDistinctSessions(from, to)
        return AnalyticsOverview(totalPageViews = totalPageViews, uniqueVisitors = uniqueVisitors)
    }

    fun getDailyPageViews(from: LocalDateTime, to: LocalDateTime): List<DailyPageViewCount> {
        return analyticsReader.findDailyPageViews(from, to)
    }

    fun getTopPages(from: LocalDateTime, to: LocalDateTime): List<PageViewCount> {
        return analyticsReader.findTopPages(from, to)
    }

    fun getTopReferrers(from: LocalDateTime, to: LocalDateTime): List<ReferrerCount> {
        return analyticsReader.findTopReferrers(from, to)
    }
}

data class AnalyticsOverview(val totalPageViews: Long, val uniqueVisitors: Long)

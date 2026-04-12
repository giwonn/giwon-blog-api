package com.giwon.blog.core.analytics.application

import com.giwon.blog.core.analytics.domain.*
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
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

    fun getDailyVisitors(from: LocalDate, to: LocalDate): List<DailyVisitorCount> {
        val today = LocalDate.now()
        val result = mutableListOf<DailyVisitorCount>()

        // 과거 데이터: daily_visitor_stats에서 조회
        if (from < today) {
            val statsTo = if (to < today) to else today.minusDays(1)
            result.addAll(analyticsReader.findDailyVisitors(from, statsTo))
        }

        // 오늘 데이터: page_views에서 실시간 카운팅
        if (to >= today) {
            val todayCount = analyticsReader.countDistinctSessions(
                today.atStartOfDay(),
                today.plusDays(1).atStartOfDay(),
            )
            result.add(DailyVisitorCount(date = today.toString(), visitorCount = todayCount))
        }

        return result
    }

    fun getTopPages(from: LocalDateTime, to: LocalDateTime): List<PageViewCount> {
        return analyticsReader.findTopPages(from, to)
    }

    fun getTopReferrers(from: LocalDateTime, to: LocalDateTime): List<ReferrerCount> {
        return analyticsReader.findTopReferrers(from, to)
    }

    fun getVisitorLocations(from: LocalDateTime, to: LocalDateTime): List<VisitorLocation> {
        return analyticsReader.findVisitorLocations(from, to)
    }

    fun getIpAccessHistory(ipAddress: String, from: LocalDateTime, to: LocalDateTime): List<IpAccessHistory> {
        return analyticsReader.findIpAccessHistory(ipAddress, from, to)
    }

    fun getArticleAccessHistory(articleId: Long, from: LocalDateTime, to: LocalDateTime): List<ArticleAccessHistory> {
        return analyticsReader.findArticleAccessHistory(articleId, from, to)
    }
}

data class AnalyticsOverview(val totalPageViews: Long, val uniqueVisitors: Long)

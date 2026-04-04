package com.giwon.blog.core.analytics.domain

import java.time.LocalDate
import java.time.LocalDateTime

interface AnalyticsReader {
    fun findTopPages(from: LocalDateTime, to: LocalDateTime): List<PageViewCount>
    fun findTopReferrers(from: LocalDateTime, to: LocalDateTime): List<ReferrerCount>
    fun findDailyPageViews(from: LocalDateTime, to: LocalDateTime): List<DailyPageViewCount>
    fun countDistinctSessions(from: LocalDateTime, to: LocalDateTime): Long
    fun sumViewCountByArticleIdSince(since: LocalDate): List<ArticleViewCount>
    fun findTopArticleStats(limit: Int): List<ArticleStatsRow>
    fun getTotalVisitorCount(): Long
    fun getVisitorCountByDate(date: LocalDate): VisitorCount
}

data class PageViewCount(val path: String, val viewCount: Long)
data class ReferrerCount(val referrer: String, val viewCount: Long)
data class DailyPageViewCount(val date: String, val viewCount: Long)
data class ArticleViewCount(val articleId: Long, val viewCount: Long)
data class ArticleStatsRow(val articleId: Long, val viewCount: Long)
data class VisitorCount(val count: Long)

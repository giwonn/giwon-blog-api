package com.giwon.blog.core.analytics.infrastructure.persistence

import com.giwon.blog.core.analytics.domain.*
import com.giwon.blog.core.analytics.domain.QDailyArticleStats.dailyArticleStats
import com.giwon.blog.core.analytics.domain.QDailyVisitorStats.dailyVisitorStats
import com.giwon.blog.core.analytics.domain.QPageView.pageView
import com.giwon.blog.core.analytics.domain.QArticleStats.articleStats
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class QueryDslAnalyticsReader(
    private val queryFactory: JPAQueryFactory,
) : AnalyticsReader {

    override fun findTopPages(from: LocalDateTime, to: LocalDateTime): List<PageViewCount> {
        return queryFactory
            .select(Projections.constructor(
                PageViewCount::class.java,
                pageView.path,
                pageView.count(),
            ))
            .from(pageView)
            .where(pageView.createdAt.between(from, to))
            .groupBy(pageView.path)
            .orderBy(pageView.count().desc())
            .fetch()
    }

    override fun findTopReferrers(from: LocalDateTime, to: LocalDateTime): List<ReferrerCount> {
        return queryFactory
            .select(Projections.constructor(
                ReferrerCount::class.java,
                pageView.referrer,
                pageView.count(),
            ))
            .from(pageView)
            .where(
                pageView.createdAt.between(from, to),
                pageView.referrer.isNotNull,
            )
            .groupBy(pageView.referrer)
            .orderBy(pageView.count().desc())
            .fetch()
    }

    override fun findDailyPageViews(from: LocalDateTime, to: LocalDateTime): List<DailyPageViewCount> {
        return queryFactory
            .select(Projections.constructor(
                DailyPageViewCount::class.java,
                pageView.createdAt.stringValue().substring(0, 10),
                pageView.count(),
            ))
            .from(pageView)
            .where(pageView.createdAt.between(from, to))
            .groupBy(pageView.createdAt.stringValue().substring(0, 10))
            .orderBy(pageView.createdAt.stringValue().substring(0, 10).asc())
            .fetch()
    }

    override fun countDistinctSessions(from: LocalDateTime, to: LocalDateTime): Long {
        return queryFactory
            .select(pageView.sessionId.countDistinct())
            .from(pageView)
            .where(
                pageView.createdAt.between(from, to),
                pageView.sessionId.isNotNull,
            )
            .fetchOne() ?: 0L
    }

    override fun sumViewCountByArticleIdSince(since: LocalDate): List<ArticleViewCount> {
        return queryFactory
            .select(Projections.constructor(
                ArticleViewCount::class.java,
                dailyArticleStats.articleId,
                dailyArticleStats.viewCount.sum(),
            ))
            .from(dailyArticleStats)
            .where(dailyArticleStats.date.goe(since))
            .groupBy(dailyArticleStats.articleId)
            .orderBy(dailyArticleStats.viewCount.sum().desc())
            .fetch()
    }

    override fun findTopArticleStats(limit: Int): List<ArticleStatsRow> {
        return queryFactory
            .select(Projections.constructor(
                ArticleStatsRow::class.java,
                articleStats.articleId,
                articleStats.viewCount,
            ))
            .from(articleStats)
            .orderBy(articleStats.viewCount.desc())
            .limit(limit.toLong())
            .fetch()
    }

    override fun getTotalVisitorCount(): Long {
        return queryFactory
            .select(dailyVisitorStats.visitorCount.sum())
            .from(dailyVisitorStats)
            .fetchOne() ?: 0L
    }

    override fun getVisitorCountByDate(date: LocalDate): VisitorCount {
        val count = queryFactory
            .select(dailyVisitorStats.visitorCount)
            .from(dailyVisitorStats)
            .where(dailyVisitorStats.date.eq(date))
            .fetchOne() ?: 0L
        return VisitorCount(count)
    }

    override fun findVisitorLocations(from: LocalDateTime, to: LocalDateTime): List<VisitorLocation> {
        return queryFactory
            .select(Projections.constructor(
                VisitorLocation::class.java,
                pageView.ipAddress,
                pageView.latitude,
                pageView.longitude,
                pageView.country,
                pageView.city,
                pageView.count(),
            ))
            .from(pageView)
            .where(
                pageView.createdAt.between(from, to),
                pageView.latitude.isNotNull,
                pageView.longitude.isNotNull,
            )
            .groupBy(pageView.ipAddress, pageView.latitude, pageView.longitude, pageView.country, pageView.city)
            .orderBy(pageView.count().desc())
            .fetch()
    }
}

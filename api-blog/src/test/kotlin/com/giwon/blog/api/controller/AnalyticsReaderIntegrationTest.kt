package com.giwon.blog.api.controller

import com.giwon.blog.api.config.TestContainersConfig
import com.giwon.blog.core.analytics.domain.AnalyticsReader
import com.giwon.blog.core.analytics.domain.DailyArticleStats
import com.giwon.blog.core.analytics.domain.PageView
import com.giwon.blog.core.analytics.infrastructure.persistence.DailyArticleStatsJpaRepository
import com.giwon.blog.core.analytics.infrastructure.persistence.PageViewJpaRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@Import(TestContainersConfig::class)
class AnalyticsReaderIntegrationTest {

    @Autowired
    lateinit var analyticsReader: AnalyticsReader

    @Autowired
    lateinit var pageViewJpaRepository: PageViewJpaRepository

    @Autowired
    lateinit var dailyArticleStatsJpaRepository: DailyArticleStatsJpaRepository

    @BeforeEach
    fun setUp() {
        pageViewJpaRepository.deleteAll()
        dailyArticleStatsJpaRepository.deleteAll()
    }

    @Test
    fun `findTopPages - 경로별 조회수를 내림차순으로 반환한다`() {
        val now = LocalDateTime.now()
        repeat(3) { pageViewJpaRepository.save(PageView(path = "/articles/1", ipAddress = "1.1.1.1", createdAt = now)) }
        repeat(1) { pageViewJpaRepository.save(PageView(path = "/articles/2", ipAddress = "1.1.1.1", createdAt = now)) }

        val result = analyticsReader.findTopPages(now.minusHours(1), now.plusHours(1))

        assertEquals(2, result.size)
        assertEquals("/articles/1", result[0].path)
        assertEquals(3L, result[0].viewCount)
    }

    @Test
    fun `countDistinctSessions - 고유 세션 수를 반환한다`() {
        val now = LocalDateTime.now()
        pageViewJpaRepository.save(PageView(path = "/a", ipAddress = "1.1.1.1", sessionId = "s1", createdAt = now))
        pageViewJpaRepository.save(PageView(path = "/b", ipAddress = "1.1.1.1", sessionId = "s1", createdAt = now))
        pageViewJpaRepository.save(PageView(path = "/a", ipAddress = "2.2.2.2", sessionId = "s2", createdAt = now))

        val count = analyticsReader.countDistinctSessions(now.minusHours(1), now.plusHours(1))

        assertEquals(2L, count)
    }

    @Test
    fun `sumViewCountByArticleIdSince - 일일 집계를 합산해서 반환한다`() {
        val today = LocalDate.now()
        dailyArticleStatsJpaRepository.save(DailyArticleStats(date = today, articleId = 1L, viewCount = 100L))
        dailyArticleStatsJpaRepository.save(DailyArticleStats(date = today.minusDays(1), articleId = 1L, viewCount = 50L))
        dailyArticleStatsJpaRepository.save(DailyArticleStats(date = today, articleId = 2L, viewCount = 30L))

        val result = analyticsReader.sumViewCountByArticleIdSince(today.minusDays(30))

        assertEquals(2, result.size)
        assertEquals(1L, result[0].articleId)
        assertEquals(150L, result[0].viewCount)
    }
}

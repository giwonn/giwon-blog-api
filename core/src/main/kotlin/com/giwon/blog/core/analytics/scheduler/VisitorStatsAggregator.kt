package com.giwon.blog.core.analytics.scheduler

import com.giwon.blog.core.analytics.domain.AnalyticsReader
import com.giwon.blog.core.analytics.domain.AnalyticsWriter
import com.giwon.blog.core.analytics.domain.DailyVisitorStats
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class VisitorStatsAggregator(
    private val analyticsReader: AnalyticsReader,
    private val analyticsWriter: AnalyticsWriter,
) {

    @Scheduled(cron = "0 5 3 * * *")
    @Transactional
    fun aggregateDaily() {
        val yesterday = LocalDate.now().minusDays(1)
        val from = yesterday.atStartOfDay()
        val to = yesterday.atTime(23, 59, 59)

        val visitorCount = analyticsReader.countDistinctSessions(from, to)

        analyticsWriter.saveDailyVisitorStats(
            DailyVisitorStats(date = yesterday, visitorCount = visitorCount)
        )
    }
}

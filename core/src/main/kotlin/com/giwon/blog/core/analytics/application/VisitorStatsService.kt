package com.giwon.blog.core.analytics.application

import com.giwon.blog.core.analytics.domain.AnalyticsReader
import com.giwon.blog.core.analytics.domain.VisitorCounter
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class VisitorStatsService(
    private val analyticsReader: AnalyticsReader,
    private val visitorCounter: VisitorCounter,
) {

    fun getVisitorSummary(): VisitorSummary {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val todayCount = visitorCounter.getVisitorCount(today)
        val yesterdayCount = visitorCounter.getVisitorCount(yesterday)
            .takeIf { it > 0 }
            ?: analyticsReader.getVisitorCountByDate(yesterday).count
        val total = analyticsReader.getTotalVisitorCount() + todayCount

        return VisitorSummary(total = total, today = todayCount, yesterday = yesterdayCount)
    }
}

data class VisitorSummary(
    val total: Long,
    val today: Long,
    val yesterday: Long,
)

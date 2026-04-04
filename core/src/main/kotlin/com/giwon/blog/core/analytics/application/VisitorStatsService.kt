package com.giwon.blog.core.analytics.application

import com.giwon.blog.core.analytics.domain.AnalyticsReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class VisitorStatsService(
    private val analyticsReader: AnalyticsReader,
) {

    fun getVisitorSummary(): VisitorSummary {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        val total = analyticsReader.getTotalVisitorCount()
        val todayCount = analyticsReader.getVisitorCountByDate(today).count
        val yesterdayCount = analyticsReader.getVisitorCountByDate(yesterday).count

        return VisitorSummary(total = total, today = todayCount, yesterday = yesterdayCount)
    }
}

data class VisitorSummary(
    val total: Long,
    val today: Long,
    val yesterday: Long,
)

package com.giwon.blog.core.analytics.application

import com.giwon.blog.core.analytics.domain.AnalyticsReader
import com.giwon.blog.core.analytics.domain.VisitorCount
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class VisitorStatsServiceTest {

    @Mock lateinit var analyticsReader: AnalyticsReader

    lateinit var visitorStatsService: VisitorStatsService

    @BeforeEach
    fun setUp() {
        visitorStatsService = VisitorStatsService(analyticsReader)
    }

    @Test
    fun `getVisitorSummary - 총 방문자, 오늘, 어제 UV를 반환한다`() {
        whenever(analyticsReader.getTotalVisitorCount()).thenReturn(1000L)
        whenever(analyticsReader.getVisitorCountByDate(LocalDate.now())).thenReturn(VisitorCount(50L))
        whenever(analyticsReader.getVisitorCountByDate(LocalDate.now().minusDays(1))).thenReturn(VisitorCount(30L))

        val result = visitorStatsService.getVisitorSummary()

        assertEquals(1000L, result.total)
        assertEquals(50L, result.today)
        assertEquals(30L, result.yesterday)
    }

    @Test
    fun `getVisitorSummary - 데이터가 없으면 0을 반환한다`() {
        whenever(analyticsReader.getTotalVisitorCount()).thenReturn(0L)
        whenever(analyticsReader.getVisitorCountByDate(any())).thenReturn(VisitorCount(0L))

        val result = visitorStatsService.getVisitorSummary()

        assertEquals(0L, result.total)
        assertEquals(0L, result.today)
        assertEquals(0L, result.yesterday)
    }
}

package com.giwon.blog.admin.controller

import com.giwon.blog.common.dto.ApiResponse
import com.giwon.blog.core.analytics.application.AnalyticsOverview
import com.giwon.blog.core.analytics.application.AnalyticsQueryService
import com.giwon.blog.core.analytics.domain.DailyPageViewCount
import com.giwon.blog.core.analytics.domain.PageViewCount
import com.giwon.blog.core.analytics.domain.ReferrerCount
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.time.LocalTime

@RestController
@RequestMapping("/admin/analytics")
class AnalyticsController(
    private val analyticsQueryService: AnalyticsQueryService,
) {

    @GetMapping("/overview")
    fun getOverview(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
    ): ApiResponse<AnalyticsOverview> {
        return ApiResponse(analyticsQueryService.getOverview(from.atStartOfDay(), to.atTime(LocalTime.MAX)))
    }

    @GetMapping("/page-views")
    fun getDailyPageViews(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
    ): ApiResponse<List<DailyPageViewCount>> {
        return ApiResponse(analyticsQueryService.getDailyPageViews(from.atStartOfDay(), to.atTime(LocalTime.MAX)))
    }

    @GetMapping("/top-pages")
    fun getTopPages(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
    ): ApiResponse<List<PageViewCount>> {
        return ApiResponse(analyticsQueryService.getTopPages(from.atStartOfDay(), to.atTime(LocalTime.MAX)))
    }

    @GetMapping("/referrers")
    fun getTopReferrers(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate,
    ): ApiResponse<List<ReferrerCount>> {
        return ApiResponse(analyticsQueryService.getTopReferrers(from.atStartOfDay(), to.atTime(LocalTime.MAX)))
    }
}

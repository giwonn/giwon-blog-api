package com.giwon.blog.core.analytics.application

import com.giwon.blog.core.analytics.domain.AnalyticsReader
import com.giwon.blog.core.analytics.domain.ArticleStatsRow
import com.giwon.blog.core.article.domain.Article
import com.giwon.blog.core.article.domain.ArticleReader
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
class PopularArticleServiceTest {

    @Mock lateinit var analyticsReader: AnalyticsReader
    @Mock lateinit var articleReader: ArticleReader

    lateinit var popularArticleService: PopularArticleService

    @BeforeEach
    fun setUp() {
        popularArticleService = PopularArticleService(analyticsReader, articleReader)
    }

    @Test
    fun `getPopularArticles - 집계 테이블에서 상위 5개 글을 반환한다`() {
        whenever(analyticsReader.findTopArticleStats(5)).thenReturn(listOf(
            ArticleStatsRow(articleId = 3L, viewCount = 100L),
            ArticleStatsRow(articleId = 1L, viewCount = 50L),
        ))
        whenever(articleReader.findById(3L)).thenReturn(Article(id = 3L, title = "글3", content = "내용"))
        whenever(articleReader.findById(1L)).thenReturn(Article(id = 1L, title = "글1", content = "내용"))

        val result = popularArticleService.getPopularArticles(5)

        assertEquals(2, result.size)
        assertEquals("글3", result[0].title)
        assertEquals(100L, result[0].viewCount)
    }

    @Test
    fun `getPopularArticles - 삭제된 글은 건너뛴다`() {
        whenever(analyticsReader.findTopArticleStats(5)).thenReturn(listOf(
            ArticleStatsRow(articleId = 1L, viewCount = 100L),
            ArticleStatsRow(articleId = 999L, viewCount = 50L),
        ))
        whenever(articleReader.findById(1L)).thenReturn(Article(id = 1L, title = "글1", content = "내용"))
        whenever(articleReader.findById(999L)).thenReturn(null)

        val result = popularArticleService.getPopularArticles(5)

        assertEquals(1, result.size)
    }
}

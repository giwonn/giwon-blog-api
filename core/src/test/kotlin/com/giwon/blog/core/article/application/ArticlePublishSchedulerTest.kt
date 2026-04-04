package com.giwon.blog.core.article.application

import com.giwon.blog.core.article.domain.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ArticlePublishSchedulerTest {

    @Mock lateinit var articleReader: ArticleReader
    @Mock lateinit var articleWriter: ArticleWriter

    lateinit var cacheManager: CacheManager
    lateinit var scheduler: ArticlePublishScheduler

    @BeforeEach
    fun setUp() {
        cacheManager = CaffeineCacheManager("articles", "articleList")
        scheduler = ArticlePublishScheduler(articleReader, articleWriter, cacheManager)
    }

    @Test
    fun `publishScheduledArticles - 예약 시간이 지난 글을 발행하고 캐시에 올린다`() {
        val article = Article(
            id = 1L, title = "예약글", content = "내용",
            status = ArticleStatus.SCHEDULED,
            publishedAt = LocalDateTime.now().minusMinutes(1),
        )
        whenever(articleReader.findScheduledBefore(any())).thenReturn(listOf(article))
        whenever(articleWriter.save(any<Article>())).thenAnswer { it.arguments[0] }

        scheduler.publishScheduledArticles()

        verify(articleWriter).save(argThat<Article> { status == ArticleStatus.PUBLISHED })

        // 캐시에 올라갔는지 확인
        val cached = cacheManager.getCache("articles")?.get(1L, Article::class.java)
        assertNotNull(cached)
        assertEquals(ArticleStatus.PUBLISHED, cached!!.status)
    }

    @Test
    fun `publishScheduledArticles - 예약 글이 없으면 아무것도 안 한다`() {
        whenever(articleReader.findScheduledBefore(any())).thenReturn(emptyList())

        scheduler.publishScheduledArticles()

        verify(articleWriter, never()).save(any())
        assertNull(cacheManager.getCache("articles")?.get(1L))
    }
}

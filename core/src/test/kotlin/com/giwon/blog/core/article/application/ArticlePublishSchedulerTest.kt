package com.giwon.blog.core.article.application

import com.giwon.blog.core.article.domain.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ArticlePublishSchedulerTest {

    @Mock lateinit var articleReader: ArticleReader
    @Mock lateinit var articleWriter: ArticleWriter

    lateinit var scheduler: ArticlePublishScheduler

    @BeforeEach
    fun setUp() {
        scheduler = ArticlePublishScheduler(articleReader, articleWriter)
    }

    @Test
    fun `publishScheduledArticles - 예약 시간이 지난 글을 PUBLISHED로 변경한다`() {
        val article = Article(
            id = 1L, title = "예약글", content = "내용",
            status = ArticleStatus.SCHEDULED,
            publishedAt = LocalDateTime.now().minusMinutes(1),
        )
        whenever(articleReader.findScheduledBefore(any())).thenReturn(listOf(article))

        scheduler.publishScheduledArticles()

        verify(articleWriter).save(argThat<Article> { status == ArticleStatus.PUBLISHED })
    }

    @Test
    fun `publishScheduledArticles - 예약 글이 없으면 아무것도 안 한다`() {
        whenever(articleReader.findScheduledBefore(any())).thenReturn(emptyList())

        scheduler.publishScheduledArticles()

        verify(articleWriter, never()).save(any())
    }
}

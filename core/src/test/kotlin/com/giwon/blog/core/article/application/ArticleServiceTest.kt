package com.giwon.blog.core.article.application

import com.giwon.blog.common.exception.BusinessException
import com.giwon.blog.core.article.domain.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ArticleServiceTest {

    @Mock lateinit var articleReader: ArticleReader
    @Mock lateinit var articleWriter: ArticleWriter
    @Mock lateinit var articleDomainService: ArticleDomainService

    lateinit var cacheManager: CacheManager
    lateinit var articleService: ArticleService

    @BeforeEach
    fun setUp() {
        cacheManager = CaffeineCacheManager("articles", "articleList")
        articleService = ArticleService(articleReader, articleWriter, articleDomainService, cacheManager)
    }

    // --- 읽기 ---

    @Test
    fun `findById - 캐시에 없으면 DB에서 조회하고 캐시에 저장한다`() {
        val article = Article(id = 1L, title = "제목", content = "내용")
        whenever(articleReader.findById(1L)).thenReturn(article)

        articleService.findById(1L)
        articleService.findById(1L)

        verify(articleReader, times(1)).findById(1L)
    }

    @Test
    fun `findById - 존재하지 않는 글 조회 시 예외 발생`() {
        whenever(articleReader.findById(999L)).thenReturn(null)
        assertThrows<BusinessException> { articleService.findById(999L) }
    }

    @Test
    fun `findAll - 목록 조회 결과를 캐시에 저장한다`() {
        val pageable = PageRequest.of(0, 10)
        whenever(articleReader.findAll(pageable)).thenReturn(PageImpl(listOf(Article(id = 1L, title = "제목", content = "내용"))))

        articleService.findAll(pageable)
        articleService.findAll(pageable)

        verify(articleReader, times(1)).findAll(pageable)
    }

    // --- 작성 ---

    @Test
    fun `create - DRAFT 상태로 저장한다`() {
        val saved = Article(id = 1L, title = "새 글", content = "내용", status = ArticleStatus.DRAFT)
        whenever(articleDomainService.processImages("내용")).thenReturn("내용")
        whenever(articleWriter.save(any<Article>())).thenReturn(saved)

        val result = articleService.create("새 글", "내용")

        assertEquals(ArticleStatus.DRAFT, result.status)
        assertNull(result.publishedAt)
    }

    @Test
    fun `create - 캐시에 저장한다 (Write-Through)`() {
        val saved = Article(id = 1L, title = "새 글", content = "내용")
        whenever(articleDomainService.processImages("내용")).thenReturn("내용")
        whenever(articleWriter.save(any<Article>())).thenReturn(saved)

        articleService.create("새 글", "내용")

        assertNotNull(cacheManager.getCache("articles")?.get(1L, Article::class.java))
    }

    // --- 수정 ---

    @Test
    fun `update - DB만 업데이트하고 캐시는 무효화한다`() {
        val article = Article(id = 1L, title = "원래", content = "원래 내용")
        whenever(articleReader.findById(1L)).thenReturn(article)
        whenever(articleDomainService.processImages("수정 내용")).thenReturn("수정 내용")
        whenever(articleWriter.save(any<Article>())).thenReturn(article)

        articleService.findById(1L)
        articleService.update(1L, "수정", "수정 내용")
        articleService.findById(1L)

        verify(articleReader, times(3)).findById(1L)
    }

    // --- 삭제 ---

    @Test
    fun `delete - DB 삭제하고 캐시도 무효화한다`() {
        val article = Article(id = 1L, title = "제목", content = "내용")
        whenever(articleReader.findById(1L)).thenReturn(article)

        articleService.findById(1L)
        articleService.delete(1L)

        assertNull(cacheManager.getCache("articles")?.get(1L))
        verify(articleWriter).delete(article)
        verify(articleDomainService).cleanupAllImages("내용")
    }

    // --- 발행 ---

    @Test
    fun `publish - DRAFT 글을 즉시 발행하고 캐시를 Write-Through한다`() {
        val article = Article(id = 1L, title = "제목", content = "내용", status = ArticleStatus.DRAFT)
        whenever(articleReader.findById(1L)).thenReturn(article)
        whenever(articleWriter.save(any<Article>())).thenAnswer { it.arguments[0] }

        val result = articleService.publish(1L)

        assertEquals(ArticleStatus.PUBLISHED, result.status)
        assertNotNull(result.publishedAt)

        // 발행된 글은 캐시에 올라가야 함 (Write-Through)
        val cached = cacheManager.getCache("articles")?.get(1L, Article::class.java)
        assertNotNull(cached)
        assertEquals(ArticleStatus.PUBLISHED, cached!!.status)
    }

    @Test
    fun `publish - 이미 발행된 글은 예외 발생`() {
        val article = Article(id = 1L, title = "제목", content = "내용", status = ArticleStatus.PUBLISHED)
        whenever(articleReader.findById(1L)).thenReturn(article)

        assertThrows<BusinessException> { articleService.publish(1L) }
    }

    // --- 예약 발행 ---

    @Test
    fun `schedule - DRAFT 글을 예약 발행한다`() {
        val article = Article(id = 1L, title = "제목", content = "내용", status = ArticleStatus.DRAFT)
        val scheduledTime = LocalDateTime.now().plusDays(1)
        whenever(articleReader.findById(1L)).thenReturn(article)
        whenever(articleWriter.save(any<Article>())).thenAnswer { it.arguments[0] }

        val result = articleService.schedule(1L, scheduledTime)

        assertEquals(ArticleStatus.SCHEDULED, result.status)
        assertEquals(scheduledTime, result.publishedAt)
    }

    @Test
    fun `schedule - 과거 시간으로 예약하면 예외 발생`() {
        val article = Article(id = 1L, title = "제목", content = "내용", status = ArticleStatus.DRAFT)
        whenever(articleReader.findById(1L)).thenReturn(article)

        assertThrows<BusinessException> {
            articleService.schedule(1L, LocalDateTime.now().minusDays(1))
        }
    }
}

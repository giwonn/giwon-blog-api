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

    // --- 조회 ---

    @Test
    fun `findById - 발행된 글은 캐시에 저장한다`() {
        val article = Article(id = 1L, title = "제목", content = "내용", publishedAt = LocalDateTime.now().minusDays(1))
        whenever(articleReader.findById(1L)).thenReturn(article)

        articleService.findById(1L)
        articleService.findById(1L)

        verify(articleReader, times(1)).findById(1L)
    }

    @Test
    fun `findById - 예약된 글은 캐시에 저장하지 않는다`() {
        val article = Article(id = 1L, title = "제목", content = "내용", publishedAt = LocalDateTime.now().plusDays(1))
        whenever(articleReader.findById(1L)).thenReturn(article)

        articleService.findById(1L)
        articleService.findById(1L)

        verify(articleReader, times(2)).findById(1L)
    }

    @Test
    fun `findById - 존재하지 않는 글 조회 시 예외`() {
        whenever(articleReader.findById(999L)).thenReturn(null)
        assertThrows<BusinessException> { articleService.findById(999L) }
    }

    @Test
    fun `findPublishedAndVisible - 발행됨 + 숨김 아닌 글만 조회`() {
        val pageable = PageRequest.of(0, 10)
        val articles = listOf(Article(id = 1L, title = "제목", content = "내용"))
        whenever(articleReader.findPublishedAndVisible(any(), any())).thenReturn(PageImpl(articles))

        val result = articleService.findPublishedAndVisible(pageable)

        assertEquals(1, result.content.size)
    }

    // --- 작성: publishedAt으로 즉시 발행 or 예약 ---

    @Test
    fun `create - 즉시 발행 (publishedAt = 현재)`() {
        val now = LocalDateTime.now()
        whenever(articleDomainService.processImages("내용")).thenReturn("내용")
        whenever(articleWriter.save(any<Article>())).thenAnswer { it.arguments[0] }

        val result = articleService.create("제목", "내용", now, false, null)

        assertTrue(result.isPublished)
    }

    @Test
    fun `create - 예약 발행 (publishedAt = 미래)`() {
        val future = LocalDateTime.now().plusDays(1)
        whenever(articleDomainService.processImages("내용")).thenReturn("내용")
        whenever(articleWriter.save(any<Article>())).thenAnswer { it.arguments[0] }

        val result = articleService.create("제목", "내용", future, false, null)

        assertTrue(result.isScheduled)
    }

    @Test
    fun `create - 즉시 발행 시 캐시에 올린다`() {
        val now = LocalDateTime.now()
        val saved = Article(id = 1L, title = "제목", content = "내용", publishedAt = now)
        whenever(articleDomainService.processImages("내용")).thenReturn("내용")
        whenever(articleWriter.save(any<Article>())).thenReturn(saved)

        articleService.create("제목", "내용", now, false, null)

        assertNotNull(cacheManager.getCache("articles")?.get(1L))
    }

    @Test
    fun `create - 예약 발행 시 캐시에 안 올린다`() {
        val future = LocalDateTime.now().plusDays(1)
        val saved = Article(id = 1L, title = "제목", content = "내용", publishedAt = future)
        whenever(articleDomainService.processImages("내용")).thenReturn("내용")
        whenever(articleWriter.save(any<Article>())).thenReturn(saved)

        articleService.create("제목", "내용", future, false, null)

        assertNull(cacheManager.getCache("articles")?.get(1L))
    }

    // --- 수정 ---

    @Test
    fun `update - 발행된 글 수정 시 캐시 Write-Through`() {
        val article = Article(id = 1L, title = "원래", content = "원래", publishedAt = LocalDateTime.now().minusDays(1))
        whenever(articleReader.findById(1L)).thenReturn(article)
        whenever(articleDomainService.processImages("수정")).thenReturn("수정")
        whenever(articleWriter.save(any<Article>())).thenReturn(article)

        articleService.update(1L, "��정", "수정", null, false, null)

        articleService.findById(1L)
        verify(articleReader, times(1)).findById(1L) // 캐시 히트
    }

    // --- 삭제 ---

    @Test
    fun `delete - 캐시에서 제거한다`() {
        val article = Article(id = 1L, title = "제목", content = "내용", publishedAt = LocalDateTime.now().minusDays(1))
        whenever(articleReader.findById(1L)).thenReturn(article)

        articleService.findById(1L)
        assertNotNull(cacheManager.getCache("articles")?.get(1L))

        articleService.delete(1L)
        assertNull(cacheManager.getCache("articles")?.get(1L))
    }

    // --- 숨김 ---

    @Test
    fun `update - hidden=true로 수정하면 캐시에서 제거`() {
        val article = Article(id = 1L, title = "제목", content = "내용", publishedAt = LocalDateTime.now().minusDays(1))
        whenever(articleReader.findById(1L)).thenReturn(article)
        whenever(articleDomainService.processImages("내용")).thenReturn("내용")
        whenever(articleWriter.save(any<Article>())).thenAnswer { it.arguments[0] }

        // 캐시에 넣기
        cacheManager.getCache("articles")?.put(1L, article)

        articleService.update(1L, "제목", "내용", null, true, null)

        assertNull(cacheManager.getCache("articles")?.get(1L))
    }

    // --- 비밀번호 ---

    @Test
    fun `findByIdForBlog - 비밀번호 보호 글은 비밀번호 없으면 예외`() {
        val article = Article(id = 1L, title = "제목", content = "내용", password = "1234", publishedAt = LocalDateTime.now().minusDays(1))
        whenever(articleReader.findById(1L)).thenReturn(article)

        assertThrows<BusinessException> { articleService.findByIdForBlog(1L, null) }
    }

    @Test
    fun `findByIdForBlog - 비밀번호 틀리면 예외`() {
        val article = Article(id = 1L, title = "제목", content = "내용", password = "1234", publishedAt = LocalDateTime.now().minusDays(1))
        whenever(articleReader.findById(1L)).thenReturn(article)

        assertThrows<BusinessException> { articleService.findByIdForBlog(1L, "wrong") }
    }

    @Test
    fun `findByIdForBlog - 비밀번호 맞으면 조회 성공`() {
        val article = Article(id = 1L, title = "제목", content = "내용", password = "1234", publishedAt = LocalDateTime.now().minusDays(1))
        whenever(articleReader.findById(1L)).thenReturn(article)

        val result = articleService.findByIdForBlog(1L, "1234")
        assertEquals("제목", result.title)
    }

    @Test
    fun `findByIdForBlog - hidden 글은 조회 불가`() {
        val article = Article(id = 1L, title = "제목", content = "내용", hidden = true, publishedAt = LocalDateTime.now().minusDays(1))
        whenever(articleReader.findById(1L)).thenReturn(article)

        assertThrows<BusinessException> { articleService.findByIdForBlog(1L, null) }
    }
}

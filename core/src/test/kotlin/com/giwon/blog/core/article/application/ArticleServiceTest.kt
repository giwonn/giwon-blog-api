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

    // --- Look-aside ---

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

    // --- Write-Through ---

    @Test
    fun `create - DB에 저장하고 캐시에도 저장한다`() {
        val saved = Article(id = 1L, title = "새 글", content = "내용")
        whenever(articleDomainService.processImages("내용")).thenReturn("내용")
        whenever(articleWriter.save(any<Article>())).thenReturn(saved)

        articleService.create("새 글", "내용")

        val cache = cacheManager.getCache("articles")
        assertNotNull(cache?.get(1L, Article::class.java))
        verify(articleReader, never()).findById(1L)
    }

    // --- Write-Around ---

    @Test
    fun `update - DB만 업데이트하고 캐시는 무효화한다`() {
        val article = Article(id = 1L, title = "원래", content = "원래 내용")
        whenever(articleReader.findById(1L)).thenReturn(article)
        whenever(articleDomainService.processImages("수정 내용")).thenReturn("수정 내용")
        whenever(articleWriter.save(any<Article>())).thenReturn(article)

        articleService.findById(1L)
        verify(articleReader, times(1)).findById(1L)

        articleService.update(1L, "수정", "수정 내용")

        // 캐시 무효화 확인: 다시 DB에서 가져옴
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
}

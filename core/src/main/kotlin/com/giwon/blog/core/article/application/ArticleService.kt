package com.giwon.blog.core.article.application

import com.giwon.blog.common.exception.BusinessException
import com.giwon.blog.common.exception.ErrorCode
import com.giwon.blog.core.article.domain.*
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ArticleService(
    private val articleReader: ArticleReader,
    private val articleWriter: ArticleWriter,
    private val articleDomainService: ArticleDomainService,
    private val cacheManager: CacheManager,
) {

    companion object {
        private const val CACHE_ARTICLES = "articles"
        private const val CACHE_ARTICLE_LIST = "articleList"
    }

    // --- 블로그용 조회 (PUBLISHED만 캐시) ---

    fun findAll(pageable: Pageable): Page<Article> {
        val cache = cacheManager.getCache(CACHE_ARTICLE_LIST) ?: return articleReader.findAll(pageable)
        val key = pageable.toString()

        @Suppress("UNCHECKED_CAST")
        val cached = cache.get(key)?.get() as? Page<Article>
        if (cached != null) return cached

        val result = articleReader.findAll(pageable)
        cache.put(key, result)
        return result
    }

    fun findAllByStatus(status: ArticleStatus, pageable: Pageable): Page<Article> {
        return articleReader.findAllByStatus(status, pageable)
    }

    fun findById(id: Long): Article {
        val cache = cacheManager.getCache(CACHE_ARTICLES)

        // 캐시에는 PUBLISHED만 있음
        val cached = cache?.get(id, Article::class.java)
        if (cached != null) return cached

        val article = articleReader.findById(id)
            ?: throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)

        // PUBLISHED만 캐시에 저장
        if (article.status == ArticleStatus.PUBLISHED) {
            cache?.put(id, article)
        }
        return article
    }

    // --- 어드민용 쓰기 ---

    @Transactional
    fun create(title: String, content: String): Article {
        val processedContent = articleDomainService.processImages(content)
        val article = Article(title = title, content = processedContent, status = ArticleStatus.DRAFT)
        return articleWriter.save(article)
        // DRAFT → 캐시 안 넣음
    }

    @Transactional
    fun update(id: Long, title: String, content: String): Article {
        val article = articleReader.findById(id)
            ?: throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)

        val processedContent = articleDomainService.processImages(content)
        articleDomainService.cleanupDeletedImages(article.content, processedContent)

        article.title = title
        article.content = processedContent
        article.updatedAt = LocalDateTime.now()
        val saved = articleWriter.save(article)

        if (saved.status == ArticleStatus.PUBLISHED) {
            // 발행된 글 수정 → 캐시 즉시 반영 (Write-Through)
            cacheManager.getCache(CACHE_ARTICLES)?.put(id, saved)
            cacheManager.getCache(CACHE_ARTICLE_LIST)?.clear()
        }
        // DRAFT/SCHEDULED 수정 → 캐시에 없으니 아무것도 안 함

        return saved
    }

    @Transactional
    fun delete(id: Long) {
        val article = articleReader.findById(id)
            ?: throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)

        articleDomainService.cleanupAllImages(article.content)
        articleWriter.delete(article)

        // 발행된 글이었을 수 있으니 캐시에서 제거
        cacheManager.getCache(CACHE_ARTICLES)?.evict(id)
        cacheManager.getCache(CACHE_ARTICLE_LIST)?.clear()
    }

    @Transactional
    fun publish(id: Long): Article {
        val article = articleReader.findById(id)
            ?: throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)

        if (article.status == ArticleStatus.PUBLISHED) {
            throw BusinessException(ErrorCode.ALREADY_PUBLISHED)
        }

        article.status = ArticleStatus.PUBLISHED
        article.publishedAt = LocalDateTime.now()
        article.updatedAt = LocalDateTime.now()
        val saved = articleWriter.save(article)

        // 발행 → 캐시에 올림 (Write-Through)
        cacheManager.getCache(CACHE_ARTICLES)?.put(id, saved)
        cacheManager.getCache(CACHE_ARTICLE_LIST)?.clear()

        return saved
    }

    @Transactional
    fun schedule(id: Long, publishedAt: LocalDateTime): Article {
        val article = articleReader.findById(id)
            ?: throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)

        if (publishedAt.isBefore(LocalDateTime.now())) {
            throw BusinessException(ErrorCode.INVALID_SCHEDULE_TIME)
        }

        article.status = ArticleStatus.SCHEDULED
        article.publishedAt = publishedAt
        article.updatedAt = LocalDateTime.now()
        return articleWriter.save(article)
        // SCHEDULED → 캐시 안 건드림
    }
}

package com.giwon.blog.core.article.application

import com.giwon.blog.common.exception.BusinessException
import com.giwon.blog.common.exception.ErrorCode
import com.giwon.blog.core.article.domain.*
import com.giwon.blog.core.image.domain.ImageStorage
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

        val cached = cache?.get(id, Article::class.java)
        if (cached != null) return cached

        val article = articleReader.findById(id)
            ?: throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)

        cache?.put(id, article)
        return article
    }

    @Transactional
    fun create(title: String, content: String): Article {
        val processedContent = articleDomainService.processImages(content)
        val article = Article(title = title, content = processedContent, status = ArticleStatus.DRAFT)
        val saved = articleWriter.save(article)

        cacheManager.getCache(CACHE_ARTICLES)?.put(saved.id, saved)
        cacheManager.getCache(CACHE_ARTICLE_LIST)?.clear()

        return saved
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

        // 발행된 글: Write-Through (즉시 반영), 그 외: Write-Around (무효화)
        if (saved.status == ArticleStatus.PUBLISHED) {
            cacheManager.getCache(CACHE_ARTICLES)?.put(id, saved)
        } else {
            cacheManager.getCache(CACHE_ARTICLES)?.evict(id)
        }
        cacheManager.getCache(CACHE_ARTICLE_LIST)?.clear()

        return saved
    }

    @Transactional
    fun delete(id: Long) {
        val article = articleReader.findById(id)
            ?: throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)

        articleDomainService.cleanupAllImages(article.content)
        articleWriter.delete(article)

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

        // Write-Through: 발행된 글은 바로 캐시에 올려서 블로그에서 즉시 보이게
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
        val saved = articleWriter.save(article)

        cacheManager.getCache(CACHE_ARTICLES)?.evict(id)
        cacheManager.getCache(CACHE_ARTICLE_LIST)?.clear()

        return saved
    }
}

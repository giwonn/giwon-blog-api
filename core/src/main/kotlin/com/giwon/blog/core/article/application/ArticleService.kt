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

    // --- 블로그용 조회 ---

    fun findPublishedAndVisible(pageable: Pageable): Page<Article> {
        val cache = cacheManager.getCache(CACHE_ARTICLE_LIST)
        val key = pageable.toString()

        @Suppress("UNCHECKED_CAST")
        val cached = cache?.get(key)?.get() as? Page<Article>
        if (cached != null) return cached

        val result = articleReader.findPublishedAndVisible(LocalDateTime.now(), pageable)
        cache?.put(key, result)
        return result
    }

    fun findByIdForBlog(id: Long, password: String?): Article {
        val article = findById(id)

        if (article.hidden) {
            throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)
        }
        if (!article.isPublished) {
            throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)
        }
        if (article.isPasswordProtected) {
            if (password == null) {
                throw BusinessException(ErrorCode.ARTICLE_PASSWORD_REQUIRED)
            }
            if (article.password != password) {
                throw BusinessException(ErrorCode.ARTICLE_PASSWORD_INCORRECT)
            }
        }

        return article
    }

    // --- 어드민용 조회 ---

    fun findAll(pageable: Pageable): Page<Article> {
        return articleReader.findAll(pageable)
    }

    fun findById(id: Long): Article {
        val cache = cacheManager.getCache(CACHE_ARTICLES)

        val cached = cache?.get(id, Article::class.java)
        if (cached != null) return cached

        val article = articleReader.findById(id)
            ?: throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)

        if (article.isVisibleOnBlog) {
            cache?.put(id, article)
        }
        return article
    }

    // --- 쓰기 ---

    @Transactional
    fun create(
        title: String,
        content: String,
        publishedAt: LocalDateTime = LocalDateTime.now(),
        hidden: Boolean = false,
        password: String? = null,
    ): Article {
        val processedContent = articleDomainService.processImages(content)
        val article = Article(
            title = title,
            content = processedContent,
            publishedAt = publishedAt,
            hidden = hidden,
            password = password,
        )
        val saved = articleWriter.save(article)

        if (saved.isVisibleOnBlog) {
            cacheManager.getCache(CACHE_ARTICLES)?.put(saved.id, saved)
            cacheManager.getCache(CACHE_ARTICLE_LIST)?.clear()
        }

        return saved
    }

    @Transactional
    fun update(
        id: Long,
        title: String,
        content: String,
        publishedAt: LocalDateTime? = null,
        hidden: Boolean = false,
        password: String? = null,
    ): Article {
        val article = articleReader.findById(id)
            ?: throw BusinessException(ErrorCode.ARTICLE_NOT_FOUND)

        val processedContent = articleDomainService.processImages(content)
        articleDomainService.cleanupDeletedImages(article.content, processedContent)

        article.title = title
        article.content = processedContent
        if (publishedAt != null) article.publishedAt = publishedAt
        article.hidden = hidden
        article.password = password
        article.updatedAt = LocalDateTime.now()
        val saved = articleWriter.save(article)

        if (saved.isVisibleOnBlog) {
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
}

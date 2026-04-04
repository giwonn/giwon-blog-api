package com.giwon.blog.core.article.application

import com.giwon.blog.common.exception.BusinessException
import com.giwon.blog.common.exception.ErrorCode
import com.giwon.blog.core.article.domain.Article
import com.giwon.blog.core.article.domain.ArticleDomainService
import com.giwon.blog.core.article.domain.ArticleReader
import com.giwon.blog.core.article.domain.ArticleWriter
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
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
        val article = Article(title = title, content = processedContent)
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

        cacheManager.getCache(CACHE_ARTICLES)?.evict(id)
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

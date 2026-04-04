package com.giwon.blog.core.article.application

import com.giwon.blog.core.article.domain.ArticleReader
import com.giwon.blog.core.article.domain.ArticleStatus
import com.giwon.blog.core.article.domain.ArticleWriter
import org.springframework.cache.CacheManager
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class ArticlePublishScheduler(
    private val articleReader: ArticleReader,
    private val articleWriter: ArticleWriter,
    private val cacheManager: CacheManager,
) {

    @Scheduled(fixedRate = 60_000)
    @Transactional
    fun publishScheduledArticles() {
        val now = LocalDateTime.now()
        val articles = articleReader.findScheduledBefore(now)

        if (articles.isEmpty()) return

        articles.forEach { article ->
            article.status = ArticleStatus.PUBLISHED
            article.updatedAt = now
            val saved = articleWriter.save(article)

            // DB 저장 후 캐시에 올림 (Write-Through)
            cacheManager.getCache("articles")?.put(saved.id, saved)
        }

        // 목록 캐시 무효화
        cacheManager.getCache("articleList")?.clear()
    }
}

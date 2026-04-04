package com.giwon.blog.core.article.application

import com.giwon.blog.core.article.domain.ArticleReader
import com.giwon.blog.core.article.domain.ArticleStatus
import com.giwon.blog.core.article.domain.ArticleWriter
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class ArticlePublishScheduler(
    private val articleReader: ArticleReader,
    private val articleWriter: ArticleWriter,
) {

    @Scheduled(fixedRate = 60_000) // 매분
    @Transactional
    fun publishScheduledArticles() {
        val now = LocalDateTime.now()
        val articles = articleReader.findScheduledBefore(now)

        articles.forEach { article ->
            article.status = ArticleStatus.PUBLISHED
            article.updatedAt = now
            articleWriter.save(article)
        }
    }
}

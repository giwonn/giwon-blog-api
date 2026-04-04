package com.giwon.blog.core.article.infrastructure.persistence

import com.giwon.blog.core.article.domain.Article
import com.giwon.blog.core.article.domain.ArticleReader
import com.giwon.blog.core.article.domain.ArticleStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class JpaArticleReader(
    private val articleJpaRepository: ArticleJpaRepository,
) : ArticleReader {

    override fun findById(id: Long): Article? {
        return articleJpaRepository.findById(id).orElse(null)
    }

    override fun findAll(pageable: Pageable): Page<Article> {
        return articleJpaRepository.findAll(pageable)
    }

    override fun findAllByStatus(status: ArticleStatus, pageable: Pageable): Page<Article> {
        return articleJpaRepository.findAllByStatus(status, pageable)
    }

    override fun findScheduledBefore(time: LocalDateTime): List<Article> {
        return articleJpaRepository.findAllByStatusAndPublishedAtBefore(ArticleStatus.SCHEDULED, time)
    }
}

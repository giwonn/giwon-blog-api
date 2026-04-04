package com.giwon.blog.core.article.infrastructure.persistence

import com.giwon.blog.core.article.domain.Article
import com.giwon.blog.core.article.domain.ArticleStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface ArticleJpaRepository : JpaRepository<Article, Long> {
    fun findAllByStatus(status: ArticleStatus, pageable: Pageable): Page<Article>
    fun findAllByStatusAndPublishedAtBefore(status: ArticleStatus, time: LocalDateTime): List<Article>
}

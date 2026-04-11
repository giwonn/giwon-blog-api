package com.giwon.blog.core.article.infrastructure.persistence

import com.giwon.blog.core.article.domain.Article
import com.giwon.blog.core.article.domain.ArticleStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface ArticleJpaRepository : JpaRepository<Article, Long> {
    fun findBySlug(slug: String): Article?
    fun existsBySlug(slug: String): Boolean
    fun findAllByStatusIn(statuses: List<ArticleStatus>, pageable: Pageable): Page<Article>
}

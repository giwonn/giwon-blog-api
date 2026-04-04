package com.giwon.blog.core.article.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface ArticleReader {
    fun findById(id: Long): Article?
    fun findAll(pageable: Pageable): Page<Article>
    fun findPublishedAndVisible(now: LocalDateTime, pageable: Pageable): Page<Article>
}

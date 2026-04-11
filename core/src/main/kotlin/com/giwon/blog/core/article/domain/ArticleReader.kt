package com.giwon.blog.core.article.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ArticleReader {
    fun findById(id: Long): Article?
    fun findBySlug(slug: String): Article?
    fun existsBySlug(slug: String): Boolean
    fun findAll(pageable: Pageable): Page<Article>
    fun findVisibleOnBlog(pageable: Pageable): Page<Article>
}

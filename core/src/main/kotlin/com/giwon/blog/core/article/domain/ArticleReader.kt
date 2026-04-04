package com.giwon.blog.core.article.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ArticleReader {
    fun findById(id: Long): Article?
    fun findAll(pageable: Pageable): Page<Article>
}

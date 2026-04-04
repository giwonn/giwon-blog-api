package com.giwon.blog.core.article.infrastructure.persistence

import com.giwon.blog.core.article.domain.Article
import org.springframework.data.jpa.repository.JpaRepository

interface ArticleJpaRepository : JpaRepository<Article, Long>

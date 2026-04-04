package com.giwon.blog.core.article.infrastructure.persistence

import com.giwon.blog.core.article.domain.Article
import com.giwon.blog.core.article.domain.ArticleReader
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

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
}

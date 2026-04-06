package com.giwon.blog.core.article.application

import com.giwon.blog.core.article.domain.Article
import java.time.LocalDateTime

data class CachedArticle(
    val id: Long,
    val title: String,
    val content: String,
    val publishedAt: LocalDateTime,
    val hidden: Boolean,
    val password: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    fun toEntity(): Article = Article(
        id = id,
        title = title,
        content = content,
        publishedAt = publishedAt,
        hidden = hidden,
        password = password,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    companion object {
        fun from(article: Article): CachedArticle = CachedArticle(
            id = article.id,
            title = article.title,
            content = article.content,
            publishedAt = article.publishedAt,
            hidden = article.hidden,
            password = article.password,
            createdAt = article.createdAt,
            updatedAt = article.updatedAt,
        )
    }
}

package com.giwon.blog.core.article.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "articles")
class Article(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Column(nullable = false)
    var publishedAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false, columnDefinition = "boolean default false")
    var hidden: Boolean = false,

    var password: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    val isPublished: Boolean get() = !publishedAt.isAfter(LocalDateTime.now())
    val isScheduled: Boolean get() = publishedAt.isAfter(LocalDateTime.now())
    val isPasswordProtected: Boolean get() = password != null
    val isVisibleOnBlog: Boolean get() = isPublished && !hidden
}

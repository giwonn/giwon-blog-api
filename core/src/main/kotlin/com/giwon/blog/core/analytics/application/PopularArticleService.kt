package com.giwon.blog.core.analytics.application

import com.giwon.blog.core.analytics.domain.AnalyticsReader
import com.giwon.blog.core.article.domain.ArticleReader
import org.springframework.stereotype.Service

@Service
class PopularArticleService(
    private val analyticsReader: AnalyticsReader,
    private val articleReader: ArticleReader,
) {

    fun getPopularArticles(limit: Int = 5): List<PopularArticle> {
        val stats = analyticsReader.findTopArticleStats(limit)

        return stats.mapNotNull { stat ->
            val article = articleReader.findById(stat.articleId) ?: return@mapNotNull null
            PopularArticle(
                id = article.id,
                title = article.title,
                viewCount = stat.viewCount,
            )
        }
    }
}

data class PopularArticle(
    val id: Long,
    val title: String,
    val viewCount: Long,
)

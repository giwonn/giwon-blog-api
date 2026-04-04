package com.giwon.blog.core.article.domain

import com.giwon.blog.core.image.domain.ImageStorage
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArticleDomainService(
    private val imageStorage: ImageStorage,
) {

    fun processImages(content: String): String {
        val base64Pattern = Regex("""!\[([^\]]*)]\(data:image/([^;]+);base64,([^)]+)\)""")
        return base64Pattern.replace(content) { match ->
            val alt = match.groupValues[1]
            val imageType = match.groupValues[2]
            val base64Data = match.groupValues[3]
            val bytes = Base64.getDecoder().decode(base64Data)
            val fileName = "${UUID.randomUUID()}.$imageType"
            val url = imageStorage.upload(fileName, bytes, "image/$imageType")
            "![$alt]($url)"
        }
    }

    fun cleanupDeletedImages(oldContent: String, newContent: String) {
        val oldUrls = extractImageUrls(oldContent)
        val newUrls = extractImageUrls(newContent)
        (oldUrls - newUrls).forEach { imageStorage.delete(it) }
    }

    fun cleanupAllImages(content: String) {
        extractImageUrls(content).forEach { imageStorage.delete(it) }
    }

    private fun extractImageUrls(content: String): Set<String> {
        val urlPattern = Regex("""!\[[^\]]*]\((https?://[^)]+)\)""")
        return urlPattern.findAll(content).map { it.groupValues[1] }.toSet()
    }
}

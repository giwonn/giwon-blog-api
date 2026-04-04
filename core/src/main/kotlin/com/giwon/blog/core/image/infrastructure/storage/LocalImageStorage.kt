package com.giwon.blog.core.image.infrastructure.storage

import com.giwon.blog.core.image.domain.ImageStorage
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Paths

@Component
class LocalImageStorage(
    @Value("\${image.storage.path:/data/blog/images}") private val storagePath: String,
    @Value("\${image.storage.public-url:http://localhost:8080/images}") private val publicUrl: String,
) : ImageStorage {

    override fun upload(name: String, data: ByteArray, contentType: String): String {
        val dir = Paths.get(storagePath)
        Files.createDirectories(dir)
        Files.write(dir.resolve(name), data)
        return "$publicUrl/$name"
    }

    override fun delete(url: String) {
        val fileName = url.substringAfterLast("/")
        val file = Paths.get(storagePath, fileName)
        Files.deleteIfExists(file)
    }
}

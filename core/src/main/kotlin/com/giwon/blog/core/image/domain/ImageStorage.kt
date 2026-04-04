package com.giwon.blog.core.image.domain

interface ImageStorage {
    fun upload(name: String, data: ByteArray, contentType: String): String
    fun delete(url: String)
}

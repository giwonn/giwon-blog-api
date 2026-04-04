package com.giwon.blog.core.article.domain

import com.giwon.blog.core.image.domain.ImageStorage
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*

@ExtendWith(MockitoExtension::class)
class ArticleDomainServiceTest {

    @Mock
    lateinit var imageStorage: ImageStorage

    lateinit var articleDomainService: ArticleDomainService

    @BeforeEach
    fun setUp() {
        articleDomainService = ArticleDomainService(imageStorage)
    }

    @Test
    fun `processImages - base64 이미지를 추출해서 업로드하고 URL로 치환한다`() {
        val content = "![alt](data:image/png;base64,iVBORw0KGgo=)"
        whenever(imageStorage.upload(any(), any(), eq("image/png"))).thenReturn("http://localhost:8080/images/test.png")

        val result = articleDomainService.processImages(content)

        verify(imageStorage).upload(any(), any(), eq("image/png"))
        assertTrue(result.contains("http://localhost:8080/images/test.png"))
        assertFalse(result.contains("base64"))
    }

    @Test
    fun `processImages - base64가 없으면 그대로 반환한다`() {
        val content = "일반 텍스트 내용"

        val result = articleDomainService.processImages(content)

        assertEquals(content, result)
        verify(imageStorage, never()).upload(any(), any(), any())
    }

    @Test
    fun `cleanupDeletedImages - 삭제된 이미지를 ImageStorage에서 제거한다`() {
        val oldContent = "![a](http://localhost:8080/images/1.png) ![b](http://localhost:8080/images/2.png)"
        val newContent = "![a](http://localhost:8080/images/1.png)"

        articleDomainService.cleanupDeletedImages(oldContent, newContent)

        verify(imageStorage).delete("http://localhost:8080/images/2.png")
        verify(imageStorage, never()).delete("http://localhost:8080/images/1.png")
    }

    @Test
    fun `cleanupAllImages - 글의 모든 이미지를 삭제한다`() {
        val content = "![a](http://localhost:8080/images/1.png) ![b](http://localhost:8080/images/2.png)"

        articleDomainService.cleanupAllImages(content)

        verify(imageStorage).delete("http://localhost:8080/images/1.png")
        verify(imageStorage).delete("http://localhost:8080/images/2.png")
    }
}

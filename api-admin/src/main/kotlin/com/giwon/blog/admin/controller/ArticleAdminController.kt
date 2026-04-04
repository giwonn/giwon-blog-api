package com.giwon.blog.admin.controller

import com.giwon.blog.common.dto.ApiResponse
import com.giwon.blog.core.article.application.ArticleService
import com.giwon.blog.core.article.domain.Article
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/admin/articles")
class ArticleAdminController(
    private val articleService: ArticleService,
) {

    @GetMapping
    fun findAll(@PageableDefault(size = 10) pageable: Pageable): ApiResponse<Page<Article>> {
        return ApiResponse(articleService.findAll(pageable))
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ApiResponse<Article> {
        return ApiResponse(articleService.findById(id))
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: ArticleRequest): ApiResponse<Article> {
        return ApiResponse(articleService.create(
            title = request.title,
            content = request.content,
            publishedAt = request.publishedAt ?: LocalDateTime.now(),
            hidden = request.hidden,
            password = request.password,
        ))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: ArticleRequest): ApiResponse<Article> {
        return ApiResponse(articleService.update(
            id = id,
            title = request.title,
            content = request.content,
            publishedAt = request.publishedAt,
            hidden = request.hidden,
            password = request.password,
        ))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) {
        articleService.delete(id)
    }
}

data class ArticleRequest(
    @field:NotBlank val title: String,
    @field:NotBlank val content: String,
    val publishedAt: LocalDateTime? = null,
    val hidden: Boolean = false,
    val password: String? = null,
)

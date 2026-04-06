package com.giwon.blog.api.controller

import com.giwon.blog.common.dto.ApiResponse
import com.giwon.blog.core.article.application.ArticleService
import com.giwon.blog.core.article.domain.Article
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/articles")
class ArticleController(
    private val articleService: ArticleService,
) {

    @GetMapping
    fun findAll(@PageableDefault(size = 10, sort = ["createdAt"], direction = Sort.Direction.DESC) pageable: Pageable): ApiResponse<Page<Article>> {
        return ApiResponse(articleService.findPublishedAndVisible(pageable))
    }

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: Long,
        @RequestParam(required = false) password: String?,
    ): ApiResponse<Article> {
        return ApiResponse(articleService.findByIdForBlog(id, password))
    }
}

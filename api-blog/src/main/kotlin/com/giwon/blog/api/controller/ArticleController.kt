package com.giwon.blog.api.controller

import com.giwon.blog.common.dto.ApiResponse
import com.giwon.blog.core.article.application.ArticleService
import com.giwon.blog.core.article.domain.Article
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/articles")
class ArticleController(
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
}

package com.giwon.blog.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableAsync
@EnableScheduling
@SpringBootApplication(scanBasePackages = ["com.giwon.blog"])
class BlogApplication

fun main(args: Array<String>) {
    runApplication<BlogApplication>(*args)
}

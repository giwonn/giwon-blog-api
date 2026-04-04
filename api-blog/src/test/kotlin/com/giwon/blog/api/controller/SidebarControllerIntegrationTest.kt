package com.giwon.blog.api.controller

import com.giwon.blog.api.config.TestContainersConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig::class)
class SidebarControllerIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun `GET sidebar_visitors - 방문자 통계를 반환한다`() {
        mockMvc.get("/sidebar/visitors")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.total") { value(0) }
                jsonPath("$.data.today") { value(0) }
                jsonPath("$.data.yesterday") { value(0) }
            }
    }

    @Test
    fun `GET sidebar_popular-articles - 인기글 목록을 반환한다`() {
        mockMvc.get("/sidebar/popular-articles")
            .andExpect {
                status { isOk() }
                jsonPath("$.data") { isArray() }
            }
    }
}

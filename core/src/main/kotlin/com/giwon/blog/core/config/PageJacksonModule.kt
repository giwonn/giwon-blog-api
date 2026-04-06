package com.giwon.blog.core.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.type.CollectionType
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class PageJacksonModule : SimpleModule() {
    init {
        addDeserializer(PageImpl::class.java, PageImplDeserializer())
    }
}

class PageImplDeserializer : JsonDeserializer<PageImpl<*>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PageImpl<*> {
        val codec = p.codec
        val node: JsonNode = codec.readTree(p)

        val contentType: CollectionType = ctxt.typeFactory
            .constructCollectionType(List::class.java, Any::class.java)
        val content: List<Any> = if (node.has("content")) {
            codec.readValue(node.get("content").traverse(codec), contentType)
        } else {
            emptyList()
        }

        val number = node.get("number")?.asInt() ?: 0
        val size = node.get("size")?.asInt() ?: 20
        val totalElements = node.get("totalElements")?.asLong() ?: content.size.toLong()

        return PageImpl(content, PageRequest.of(number, size), totalElements)
    }
}

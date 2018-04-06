package com.erfangc.sesamelab

import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("api/v1")
class TaggingController(private val taggingService: TaggingService) {
    @PostMapping("tag")
    fun train(@RequestBody text: String, @RequestParam corpus: String, @RequestParam id: String?, principal: Principal?): String {
        return taggingService.tag(id, text, principal?.name ?: "anonymous", corpus)
    }
}
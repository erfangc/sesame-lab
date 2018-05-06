package com.erfangc.sesamelab.document

import java.time.Instant

data class Document(
        val id: String?,
        val content: String,
        val corpusID: Long,
        val creatorID: String,
        val creatorEmail: String,
        val createdOn: Instant,
        val lastModifiedOn: Instant,
        val lastModifiedUserID: String,
        val lastModifiedUserEmail: String,
        val entities: List<TaggedEntity> = emptyList()
)

data class TaggedEntity(val type: String, val value: String)
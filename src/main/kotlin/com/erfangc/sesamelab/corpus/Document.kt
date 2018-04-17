package com.erfangc.sesamelab.corpus

data class Document(
        val id: String?,
        val content: String,
        val corpus: String,
        val createdOn: Long,
        val createdBy: String,
        val createdByEmail: String,
        val createdByNickname: String,
        val lastModifiedOn: Long,
        val lastModifiedBy: String,
        val lastModifiedByEmail: String,
        val lastModifiedByNickname: String,
        val entities: List<TaggedEntity> = emptyList()
)

data class TaggedEntity(val type: String, val value: String)
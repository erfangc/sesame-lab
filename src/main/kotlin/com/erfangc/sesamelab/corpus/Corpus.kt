package com.erfangc.sesamelab.corpus

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
data class Corpus(
        @Id
        val id: String,
        val title: String,
        @Column(name = "user_id")
        val userID: String
)

@Entity
@Table(name = "entity_configurations")
data class EntityConfiguration(
        @Id
        val id: String = "",
        val color: String,
        val textColor: String,
        val displayName: String,
        val type: String,
        @Column(name = "corpus_id")
        val corpusID: String
)
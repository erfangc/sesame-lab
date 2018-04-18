package com.erfangc.sesamelab.corpus

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EntityConfigurationsRepository : JpaRepository<EntityConfiguration, String> {
    fun deleteByCorpusID(corpusID: String): List<EntityConfiguration>
    fun findByCorpusID(corpusID: String): List<EntityConfiguration>
}
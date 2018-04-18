package com.erfangc.sesamelab.corpus

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import javax.transaction.Transactional

@Service
@Transactional
class CorpusService(private val corpusRepository: CorpusRepository,
                    private val entityConfigurationsRepository: EntityConfigurationsRepository,
                    private val jdbcTemplate: JdbcTemplate) {
    fun save(@RequestBody corpus: Corpus) {
        corpusRepository.save(corpus)
    }

    fun saveEntity(@RequestBody entityConfigration: EntityConfiguration) {
        entityConfigurationsRepository.save(entityConfigration)
    }

    fun deleteEntity(entityID: String) {
        entityConfigurationsRepository.deleteById(entityID)
    }

    fun delete(id: String) {
        entityConfigurationsRepository.deleteByCorpusID(corpusID = id)
        corpusRepository.deleteById(id)
    }

    fun getAll(): List<CorpusDescriptor> {
        return jdbcTemplate
                .queryForList(
                        """
                SELECT
                    entity_configurations.*,
                    corpus.title,
                    corpus.user_id
                FROM
                    corpus LEFT OUTER JOIN entity_configurations  ON corpus.id = entity_configurations.corpus_id
                """.trimIndent()
                )
                .groupBy { it["corpus_id"] }
                .map { entry ->
                    val title = entry.value[0]["title"].toString()
                    val entityConfigs = entry.value.map {
                        val type = it["type"].toString()
                        type to EntityConfiguration(
                                id = it["id"].toString(),
                                type = type,
                                displayName = it["display_name"].toString(),
                                corpusID = it["corpus_id"].toString(),
                                textColor = it["text_color"].toString(),
                                color = it["color"].toString()
                        )
                    }.toMap()
                    CorpusDescriptor(id = entry.key.toString(), title = title, entityConfigs = entityConfigs)
                }
    }
}
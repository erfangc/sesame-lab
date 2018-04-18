package com.erfangc.sesamelab.corpus

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestBody
import java.sql.SQLException
import java.sql.SQLIntegrityConstraintViolationException
import javax.transaction.Transactional

/**
 * performs CRUD operations on Corpus metadata and entity configurations
 */
@Service
@Transactional
class CorpusService(private val corpusRepository: CorpusRepository,
                    private val entityConfigurationsRepository: EntityConfigurationsRepository,
                    private val jdbcTemplate: JdbcTemplate) {
    fun save(corpus: Corpus): Corpus {
        return corpusRepository.save(corpus)
    }

    fun saveEntity(entityConfigration: EntityConfiguration): EntityConfiguration {
        return entityConfigurationsRepository.save(entityConfigration)
    }

    fun deleteEntity(entityID: String) {
        entityConfigurationsRepository.deleteById(entityID)
    }

    fun getOne(id: String): CorpusDescriptor {
        val corpus = corpusRepository.getOne(id)
        val entityConfigs = entityConfigurationsRepository.findByCorpusID(corpusID = corpus.id)
        return CorpusDescriptor(
                id = corpus.id,
                title = corpus.title,
                userID = corpus.userID,
                entityConfigs = entityConfigs.associateBy { it.type }
        )
    }

    fun delete(id: String) {
        try {
            entityConfigurationsRepository.deleteByCorpusID(corpusID = id)
            corpusRepository.deleteById(id)
        } catch (ex: Exception) {
            throw RuntimeException(
                    "We cannot delete this corpus because there are associated NLP models trained against it"
            )
        }

    }

    fun getAll(): List<CorpusDescriptor> {
        return jdbcTemplate
                .queryForList(
                        """
                SELECT
                    entity_configurations.id AS `entity_id`,
                    color,
                    text_color,
                    display_name,
                    type,
                    corpus.id AS `corpus_id`,
                    corpus.title,
                    corpus.user_id
                FROM
                    corpus LEFT OUTER JOIN entity_configurations ON corpus.id = entity_configurations.corpus_id
                """.trimIndent()
                )
                .groupBy { it["corpus_id"] }
                .map { entry ->
                    val title = entry.value[0]["title"].toString()
                    val userID = entry.value[0]["user_id"].toString()
                    val entityConfigs = entry.value.filter { it["entity_id"] != null }.map {
                        val type = it["type"].toString()
                        type to EntityConfiguration(
                                id = it["entity_id"].toString(),
                                type = type,
                                displayName = it["display_name"].toString(),
                                corpusID = it["corpus_id"].toString(),
                                textColor = it["text_color"].toString(),
                                color = it["color"].toString()
                        )
                    }.toMap()
                    CorpusDescriptor(id = entry.key.toString(), title = title, userID = userID, entityConfigs = entityConfigs)
                }
    }
}
package com.erfangc.sesamelab.corpus

import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("api/v1/corpus")
class CorpusController(private val corpusService: CorpusService) {
    @PutMapping
    fun save(@RequestBody corpus: Corpus) {
        corpusService.save(corpus)
    }

    @PutMapping("entity-configuration")
    fun saveEntity(@RequestBody entityConfigration: EntityConfiguration) {
        corpusService.saveEntity(entityConfigration)
    }

    @DeleteMapping("entity-configuration")
    fun deleteEntity(entityID: String) {
        corpusService.deleteEntity(entityID)
    }

    @DeleteMapping("{id}")
    fun delete(@PathVariable id: String) {
        corpusService.delete(id)
    }

    @GetMapping
    fun getAll(): List<CorpusDescriptor> {
        return corpusService.getAll()
    }

}
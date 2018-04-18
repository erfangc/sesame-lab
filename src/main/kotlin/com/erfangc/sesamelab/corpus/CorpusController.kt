package com.erfangc.sesamelab.corpus

import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("api/v1/corpus")
class CorpusController(private val corpusService: CorpusService) {
    @PutMapping
    fun save(@RequestBody corpus: Corpus): Corpus {
        return corpusService.save(corpus)
    }

    @PutMapping("entity-configuration")
    fun saveEntity(@RequestBody entityConfigration: EntityConfiguration): EntityConfiguration {
        return corpusService.saveEntity(entityConfigration)
    }

    @DeleteMapping("entity-configuration/{entityID}")
    fun deleteEntity(@PathVariable entityID: String) {
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

    @GetMapping("{id}")
    fun getOne(@PathVariable id: String): CorpusDescriptor {
        return corpusService.getOne(id)
    }

}
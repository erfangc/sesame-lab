package com.erfangc.sesamelab.corpus.repositories;

import com.erfangc.sesamelab.corpus.entities.Corpus;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@RepositoryRestResource
public interface CorpusRepository extends PagingAndSortingRepository<Corpus, Long> {
}

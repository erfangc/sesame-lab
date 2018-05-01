package com.erfangc.sesamelab.model.repositories;

import com.erfangc.sesamelab.model.entities.NERModel;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "nermodels", path = "nermodels")
public interface NERModelRepository extends PagingAndSortingRepository<NERModel, Long> {
}

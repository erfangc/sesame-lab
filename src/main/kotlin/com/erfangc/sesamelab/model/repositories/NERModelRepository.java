package com.erfangc.sesamelab.model.repositories;

import com.erfangc.sesamelab.model.entities.NERModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "nermodels", path = "nermodels")
public interface NERModelRepository extends JpaRepository<NERModel, Long> {
}

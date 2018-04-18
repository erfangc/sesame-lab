package com.erfangc.sesamelab.corpus

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CorpusRepository: JpaRepository<Corpus, String>
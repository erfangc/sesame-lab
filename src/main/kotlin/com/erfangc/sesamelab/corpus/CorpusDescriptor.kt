package com.erfangc.sesamelab.corpus

data class CorpusDescriptor(val id: String, val title: String, val userID: String, val entityConfigs: Map<String, EntityConfiguration>)
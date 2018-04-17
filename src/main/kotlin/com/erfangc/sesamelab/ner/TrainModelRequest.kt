package com.erfangc.sesamelab.ner

import com.erfangc.sesamelab.user.User

data class TrainModelRequest(val user: User, val name: String, val description: String?, val corpusID: String, val modifiedAfter: Long)
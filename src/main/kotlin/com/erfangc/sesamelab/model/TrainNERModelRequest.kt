package com.erfangc.sesamelab.model

import com.erfangc.sesamelab.user.User

data class TrainNERModelRequest(val user: User, val name: String, val description: String?, val corpusID: String, val modifiedAfter: Long)
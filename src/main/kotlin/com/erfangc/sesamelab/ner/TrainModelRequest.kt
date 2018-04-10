package com.erfangc.sesamelab.ner

import com.erfangc.sesamelab.user.User

data class TrainModelRequest(val user: User, val modelName: String, val modelDescription: String?, val corpus: String, val modifiedAfter: Long)
package com.chesnovitskiy.chatapp.data

data class Message(
    val author: String? = null,
    val message: String? = null,
    val date: Long? = null,
    val imageUrl: String? = null
)
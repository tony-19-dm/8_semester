package com.example.astronomy

data class NewsItem(
    val id: Int,
    val title: String,
    val content: String,
    val totalLikes: Int = 0,
    val canLike: Boolean = true
)
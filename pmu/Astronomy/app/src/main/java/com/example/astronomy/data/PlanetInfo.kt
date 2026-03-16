package com.example.astronomy.data

data class PlanetInfo(
    val name: String,           // Название планеты
    val description: String,     // Описание
    val imageResId: Int,         // Ресурс картинки
    val color: Int               // Цвет для оформления (опционально)
)
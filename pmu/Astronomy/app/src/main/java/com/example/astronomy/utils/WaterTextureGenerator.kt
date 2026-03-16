package com.example.astronomy.utils

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*

class WaterTextureGenerator {

    companion object {
        /**
         * Генерирует текстуру водной глади с волнами
         * @param width ширина текстуры
         * @param height высота текстуры
         * @param time время для анимации (0..2π)
         * @return Bitmap с водной текстурой
         */
        fun generateWaterTexture(width: Int = 512, height: Int = 512, time: Float = 0f): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            // Основные цвета воды
            val deepWater = Color.rgb(0, 40, 80)      // глубокий тёмно-синий
            val mediumWater = Color.rgb(0, 80, 140)   // средний синий
            val lightWater = Color.rgb(100, 180, 220) // светлая вода (пена/блики)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    // Нормализованные координаты (0..1)
                    val nx = x.toFloat() / width
                    val ny = y.toFloat() / height

                    // Генерируем несколько волн с разными частотами и фазами

                    // Волна 1: крупные волны по горизонтали
                    val wave1 = sin(nx * 20f + time * 5f) * cos(ny * 8f + time * 2f)

                    // Волна 2: средние волны по диагонали
                    val wave2 = sin(nx * 40f + ny * 30f + time * 8f) * 0.5f

                    // Волна 3: мелкая рябь
                    val wave3 = sin(nx * 80f + time * 15f) * cos(ny * 70f + time * 10f) * 0.3f

                    // Объединяем волны (значение от -1 до 1)
                    val combinedWave = (wave1 + wave2 + wave3) / 2.5f

                    // Нормализуем в диапазон 0..1
                    val waveIntensity = (combinedWave + 1f) / 2f

                    // Создаём градиент от глубокого к светлому на основе интенсивности волны
                    val color = when {
                        waveIntensity < 0.3f -> deepWater
                        waveIntensity < 0.6f -> mediumWater
                        else -> lightWater
                    }

                    // Добавляем небольшие вариации в цвет для большей реалистичности
                    val r = (Color.red(color) * (0.9f + 0.2f * waveIntensity)).toInt().coerceIn(0, 255)
                    val g = (Color.green(color) * (0.9f + 0.2f * waveIntensity)).toInt().coerceIn(0, 255)
                    val b = (Color.blue(color) * (1.1f - 0.2f * waveIntensity)).toInt().coerceIn(0, 255)

                    bitmap.setPixel(x, y, Color.rgb(r, g, b))
                }
            }

            return bitmap
        }

        /**
         * Упрощённая версия с синусоидальными волнами
         */
        fun generateSimpleWaterTexture(width: Int = 512, height: Int = 512, time: Float = 0f): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    val nx = x.toFloat() / width
                    val ny = y.toFloat() / height

                    // Синусоидальные волны в двух направлениях
                    val waveX = sin(nx * 30f + time * 4f) * cos(ny * 20f)
                    val waveY = cos(ny * 40f + time * 6f) * sin(nx * 15f)

                    val intensity = (waveX + waveY + 2f) / 4f // нормализация 0..1

                    // Голубой цвет с вариациями
                    val r = (40 + 40 * intensity).toInt()
                    val g = (80 + 100 * intensity).toInt()
                    val b = (180 + 60 * intensity).toInt()

                    bitmap.setPixel(x, y, Color.rgb(r, g, b))
                }
            }

            return bitmap
        }

        /**
         * Самый простой вариант - градиент с волнистыми линиями
         */
        fun generateWaveLinesTexture(width: Int = 512, height: Int = 512): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    val nx = x.toFloat() / width
                    val ny = y.toFloat() / height

                    // Создаём волнистые горизонтальные линии
                    val wave = sin(nx * 40f + ny * 10f) * 0.1f + ny

                    val blueShade = (150 + 100 * wave).toInt().coerceIn(0, 255)
                    val greenShade = (100 + 80 * wave).toInt().coerceIn(0, 255)

                    bitmap.setPixel(x, y, Color.rgb(50, greenShade, blueShade))
                }
            }

            return bitmap
        }
    }
}
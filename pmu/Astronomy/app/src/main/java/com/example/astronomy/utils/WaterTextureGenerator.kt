package com.example.astronomy.utils

import android.graphics.Bitmap
import android.graphics.Color
import kotlin.math.*

class WaterTextureGenerator {

    companion object {
        fun generateWaterTexture(width: Int = 512, height: Int = 512, time: Float = 0f): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            // Основные цвета воды
            val deepWater = Color.rgb(0, 40, 80)      // глубокий тёмно-синий
            val mediumWater = Color.rgb(0, 80, 140)   // средний синий
            val lightWater = Color.rgb(100, 180, 220) // светлая вода

            for (x in 0 until width) {
                for (y in 0 until height) {
                    // Нормализованные координаты (0..1)
                    val nx = x.toFloat() / width
                    val ny = y.toFloat() / height

                    val wave1 = sin(nx * 20f + time * 5f) * cos(ny * 8f + time * 2f)

                    val wave2 = sin(nx * 40f + ny * 30f + time * 8f) * 0.5f

                    val wave3 = sin(nx * 80f + time * 15f) * cos(ny * 70f + time * 10f) * 0.3f

                    val combinedWave = (wave1 + wave2 + wave3) / 2.5f

                    // Нормализуем в диапазон 0..1
                    val waveIntensity = (combinedWave + 1f) / 2f

                    val color = when {
                        waveIntensity < 0.3f -> deepWater
                        waveIntensity < 0.6f -> mediumWater
                        else -> lightWater
                    }

                    val r = (Color.red(color) * (0.9f + 0.2f * waveIntensity)).toInt().coerceIn(0, 255)
                    val g = (Color.green(color) * (0.9f + 0.2f * waveIntensity)).toInt().coerceIn(0, 255)
                    val b = (Color.blue(color) * (1.1f - 0.2f * waveIntensity)).toInt().coerceIn(0, 255)

                    bitmap.setPixel(x, y, Color.rgb(r, g, b))
                }
            }

            return bitmap
        }
    }
}
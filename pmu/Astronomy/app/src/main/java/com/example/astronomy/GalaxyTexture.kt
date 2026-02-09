package com.example.astronomy

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.random.Random

object GalaxyTexture {
    fun createGalaxyTexture(width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint()

        // Заливаем черным фоном
        canvas.drawColor(Color.BLACK)

        // Рисуем звезды
        paint.color = Color.WHITE
        val random = Random(System.currentTimeMillis())

        for (i in 0 until 500) {
            val x = random.nextFloat() * width
            val y = random.nextFloat() * height
            val radius = random.nextFloat() * 3
            canvas.drawCircle(x, y, radius, paint)
        }

        // Добавляем туманности
        paint.color = Color.argb(100, 100, 50, 200) // Фиолетовая туманность
        canvas.drawCircle(width * 0.3f, height * 0.3f, width * 0.2f, paint)

        paint.color = Color.argb(80, 200, 100, 50) // Оранжевая туманность
        canvas.drawCircle(width * 0.7f, height * 0.7f, width * 0.15f, paint)

        return bitmap
    }
}
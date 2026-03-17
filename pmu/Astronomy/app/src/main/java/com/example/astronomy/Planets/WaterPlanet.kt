package com.example.astronomy.Planets

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.example.astronomy.utils.WaterTextureGenerator

class WaterPlanet(
    context: Context,
    radius: Float,
    stacks: Int = 48,
    slices: Int = 48,
    color: FloatArray = floatArrayOf(0.0f, 0.0f, 0.8f, 1.0f)
) : Planet(radius, stacks, slices, color) {

    private var animationTime = 0f
    private val ctx = context

    // Переопределяем метод отрисовки для использования текстуры
    fun drawWithWater(program: Int, mvpMatrixHandle: Int, modelMatrixHandle: Int,
                      positionHandle: Int, normalHandle: Int, texCoordHandle: Int,
                      textureUniformHandle: Int) {

        // Генерируем новую текстуру с волнами (анимированную)
        updateTexture()

        // Используем текстуру при отрисовке
        drawWithTexture(program, mvpMatrixHandle, modelMatrixHandle,
            positionHandle, normalHandle, texCoordHandle, textureUniformHandle)
    }

    private fun updateTexture() {
        // Увеличиваем время для анимации
        animationTime += 0.1f
        if (animationTime > 2f * Math.PI.toFloat()) {
            animationTime -= 2f * Math.PI.toFloat()
        }

        // Генерируем новую текстуру воды
        val waterBitmap = WaterTextureGenerator.generateWaterTexture(512, 512, animationTime)

        // Загружаем в OpenGL (перезаписываем существующую текстуру)
        if (textureId == 0) {
            textureId = createTexture()
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, waterBitmap, 0)
        waterBitmap.recycle()
    }

    private fun createTexture(): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

        // Настройки текстуры
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)

        return textureIds[0]
    }
}
package com.example.astronomy.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

class SimpleBlackHole {

    private val segments = 64
    private val vertices: FloatArray
    private val vertexBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val indexCount: Int

    init {
        // Вершины: центр + точки по окружности
        vertices = FloatArray((segments + 2) * 3)

        // Центр круга (0,0,0)
        vertices[0] = 0f
        vertices[1] = 0f
        vertices[2] = 0f

        // Точки на окружности
        for (i in 0..segments) {
            val angle = 2f * PI.toFloat() * i / segments
            val index = (i + 1) * 3
            vertices[index] = cos(angle)      // x
            vertices[index + 1] = sin(angle)  // y
            vertices[index + 2] = 0f           // z
        }

        // Буфер вершин
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        // Индексы для треугольников
        val indices = ShortArray(segments * 3)
        for (i in 0 until segments) {
            indices[i * 3] = 0
            indices[i * 3 + 1] = (i + 1).toShort()
            indices[i * 3 + 2] = (i + 2).toShort()
        }
        indexCount = indices.size

        // Буфер индексов
        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indices)
                position(0)
            }
        }
    }

    fun drawWithProgram(program: Int, mvpMatrixHandle: Int, positionHandle: Int) {
        GLES20.glUseProgram(program)

        // Включаем прозрачность
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Подключаем буфер вершин
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        // Рисуем
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        // Отключаем
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisable(GLES20.GL_BLEND)
    }
}
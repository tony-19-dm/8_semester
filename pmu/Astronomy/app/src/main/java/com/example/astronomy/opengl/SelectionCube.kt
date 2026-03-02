package com.example.astronomy.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class SelectionCube {

    // Вершины куба (8 вершин)
    private val vertices = floatArrayOf(
        -1.0f, -1.0f,  1.0f, // 0 передняя левая нижняя
        1.0f, -1.0f,  1.0f, // 1 передняя правая нижняя
        -1.0f,  1.0f,  1.0f, // 2 передняя левая верхняя
        1.0f,  1.0f,  1.0f, // 3 передняя правая верхняя
        -1.0f, -1.0f, -1.0f, // 4 задняя левая нижняя
        1.0f, -1.0f, -1.0f, // 5 задняя правая нижняя
        -1.0f,  1.0f, -1.0f, // 6 задняя левая верхняя
        1.0f,  1.0f, -1.0f  // 7 задняя правая верхняя
    )

    // Цвет граней: тёмно-синий полупрозрачный
    private val faceColor = floatArrayOf(0.0f, 0.0f, 0.5f, 0.4f) // увеличил прозрачность до 40%

    // Цвет рёбер: бирюзовый
    private val edgeColor = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f)

    // Индексы для граней (6 граней * 2 треугольника)
    private val faceIndices = shortArrayOf(
        // Передняя грань
        0, 1, 2, 1, 3, 2,
        // Задняя грань
        5, 4, 7, 4, 6, 7,
        // Левая грань
        4, 0, 6, 0, 2, 6,
        // Правая грань
        1, 5, 3, 5, 7, 3,
        // Верхняя грань
        2, 3, 6, 3, 7, 6,
        // Нижняя грань
        4, 5, 0, 5, 1, 0
    )

    // Индексы для рёбер
    private val edgeIndices = intArrayOf(
        0, 1, 1, 3, 3, 2, 2, 0, // передние
        4, 5, 5, 7, 7, 6, 6, 4, // задние
        0, 4, 1, 5, 3, 7, 2, 6  // соединительные
    )

    private val vertexBuffer: FloatBuffer
    private val faceIndexBuffer: ShortBuffer

    init {
        // Буфер вершин
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        // Буфер индексов для граней
        faceIndexBuffer = ByteBuffer.allocateDirect(faceIndices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(faceIndices)
                position(0)
            }
        }
    }

    fun draw(program: Int, mvpMatrixHandle: Int, positionHandle: Int, colorUniformHandle: Int) {
        // Включаем прозрачность
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Отключаем запись в глубину для полупрозрачных объектов (чтобы они не скрывали друг друга)
        GLES20.glDepthMask(false)

        // Подключаем вершинный буфер
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        // --- Рисуем грани (полупрозрачные) ---
        GLES20.glUniform4fv(colorUniformHandle, 1, faceColor, 0)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, faceIndices.size, GLES20.GL_UNSIGNED_SHORT, faceIndexBuffer)

        // --- Рисуем рёбра (непрозрачные) ---
        GLES20.glUniform4fv(colorUniformHandle, 1, edgeColor, 0)

        // Рисуем линии потолще для лучшей видимости
        GLES20.glLineWidth(3.0f)
        GLES20.glDrawElements(GLES20.GL_LINES, edgeIndices.size, GLES20.GL_UNSIGNED_INT,
            java.nio.IntBuffer.wrap(edgeIndices))

        // Возвращаем настройки глубины
        GLES20.glDepthMask(true)

        // Отключаем
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisable(GLES20.GL_BLEND)
    }
}
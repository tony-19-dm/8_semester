package com.example.astronomy.opengl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Square {

    // Вершины: X, Y, Z,   U, V (текстурные координаты)
    private val vertices = floatArrayOf(
        // Позиция          // Текстурные коорд.
        -1.0f, -1.0f, 0.0f, 0.0f, 0.0f, // нижн лев
        1.0f, -1.0f, 0.0f, 1.0f, 0.0f, // нижн прав
        -1.0f,  1.0f, 0.0f, 0.0f, 1.0f, // верх лев
        1.0f,  1.0f, 0.0f, 1.0f, 1.0f  // верх прав
    )

    // Индексы для отрисовки (2 треугольника)
    private val drawOrder = shortArrayOf(0, 1, 2, 1, 3, 2)

    val vertexBuffer: FloatBuffer
    val indexBuffer: ShortBuffer

    init {
        // Буфер вершин (5 компонентов * 4 байта)
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        // Буфер индексов (short = 2 байта)
        indexBuffer = ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }
    }
}
package com.example.astronomy.opengl

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Cube {

    private val vertices = floatArrayOf(
        // Передняя грань (Z = 0.5)
        -0.5f, -0.5f,  0.5f, // 0
        0.5f, -0.5f,  0.5f, // 1
        -0.5f,  0.5f,  0.5f, // 2
        0.5f,  0.5f,  0.5f, // 3

        // Задняя грань (Z = -0.5)
        -0.5f, -0.5f, -0.5f, // 4
        0.5f, -0.5f, -0.5f, // 5
        -0.5f,  0.5f, -0.5f, // 6
        0.5f,  0.5f, -0.5f, // 7

        // Левая грань (X = -0.5) - переиспользуем вершины 0,2,4,6
        // Правая грань (X = 0.5) - вершины 1,3,5,7
        // Верх (Y = 0.5) - вершины 2,3,6,7
        // Низ (Y = -0.5) - вершины 0,1,4,5
    )

    // Индексы для 6 граней * 2 треугольника = 12 треугольников
    private val drawOrder = shortArrayOf(
        // Перед
        0, 1, 2,  1, 3, 2,
        // Зад
        5, 4, 7,  4, 6, 7,
        // Лев
        4, 0, 6,  0, 2, 6,
        // Прав
        1, 5, 3,  5, 7, 3,
        // Верх
        2, 3, 6,  3, 7, 6,
        // Низ
        4, 5, 0,  5, 1, 0
    )

    val vertexBuffer: FloatBuffer
    val indexBuffer: ShortBuffer

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        indexBuffer = ByteBuffer.allocateDirect(drawOrder.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(drawOrder)
                position(0)
            }
        }
    }
}
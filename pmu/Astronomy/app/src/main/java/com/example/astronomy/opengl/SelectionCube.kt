package com.example.astronomy.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

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

    // Цвета для каждого ребра (разные, чтобы было видно вращение)
    private val edgeColors = floatArrayOf(
        1.0f, 0.0f, 0.0f, 1.0f, // красный
        0.0f, 1.0f, 0.0f, 1.0f, // зелёный
        0.0f, 0.0f, 1.0f, 1.0f, // синий
        1.0f, 1.0f, 0.0f, 1.0f, // жёлтый
        1.0f, 0.0f, 1.0f, 1.0f, // пурпурный
        0.0f, 1.0f, 1.0f, 1.0f, // голубой
        1.0f, 0.5f, 0.0f, 1.0f, // оранжевый
        0.5f, 0.0f, 1.0f, 1.0f  // фиолетовый
    )

    // Индексы для рёбер (каждое ребро - 2 вершины)
    private val edgeIndices = intArrayOf(
        0, 1, // переднее нижнее
        1, 3, // переднее правое
        3, 2, // переднее верхнее
        2, 0, // переднее левое
        4, 5, // заднее нижнее
        5, 7, // заднее правое
        7, 6, // заднее верхнее
        6, 4, // заднее левое
        0, 4, // левое нижнее соединительное
        1, 5, // правое нижнее соединительное
        2, 6, // левое верхнее соединительное
        3, 7  // правое верхнее соединительное
    )

    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer

    init {
        // Буфер вершин
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        // Буфер цветов для рёбер
        val colors = FloatArray(edgeIndices.size * 4) // 4 компонента на каждое ребро
        for (i in edgeIndices.indices step 2) {
            val colorIndex = (i / 2) % edgeColors.size / 4
            val color = edgeColors.sliceArray(colorIndex * 4 until colorIndex * 4 + 4)
            for (j in 0 until 2) { // на каждую вершину ребра
                colors[i * 2 + j * 4] = color[0]
                colors[i * 2 + j * 4 + 1] = color[1]
                colors[i * 2 + j * 4 + 2] = color[2]
                colors[i * 2 + j * 4 + 3] = color[3]
            }
        }

        colorBuffer = ByteBuffer.allocateDirect(colors.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(colors)
                position(0)
            }
        }
    }

    fun draw(program: Int, mvpMatrixHandle: Int, positionHandle: Int, colorHandle: Int) {
        // Включаем прозрачность
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Вершины
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        // Рисуем каждое ребро отдельно с разными цветами
        colorBuffer.position(0)
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)

        // Рисуем линии
        GLES20.glDrawElements(GLES20.GL_LINES, edgeIndices.size, GLES20.GL_UNSIGNED_INT,
            java.nio.IntBuffer.wrap(edgeIndices))

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(colorHandle)
        GLES20.glDisable(GLES20.GL_BLEND)
    }
}
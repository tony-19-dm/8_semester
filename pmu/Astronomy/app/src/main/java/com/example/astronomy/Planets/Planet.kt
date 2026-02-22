package com.example.astronomy.Planets

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Planet(
    private val radius: Float,
    private val stacks: Int = 48,
    private val slices: Int = 48,
    private val color: FloatArray = floatArrayOf(1f, 1f, 1f, 1f) // RGBA по умолчанию
) {
    private var vertexBuffer: FloatBuffer
    private var normalBuffer: FloatBuffer
    private var indexBuffer: java.nio.ShortBuffer
    private val indexCount: Int

    // Параметры движения
    var orbitRadius: Float = 0f        // радиус орбиты
    var orbitSpeed: Float = 0f         // скорость орбитального вращения
    var rotationSpeed: Float = 0f      // скорость вращения вокруг оси
    var orbitAngle: Float = 0f         // текущий угол на орбите
    var rotationAngle: Float = 0f      // текущий угол поворота вокруг оси

    // Специальный режим для Луны (вращение перпендикулярно эклиптике)
    var isMoon: Boolean = false
    var moonOrbitAngle: Float = 0f      // угол для перпендикулярного вращения

    init {
        // Генерируем сферу [citation:1]
        val vertices = generateVertices()
        val normals = generateNormals(vertices)
        val indices = generateIndices()

        indexCount = indices.size

        // Буфер вершин
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        // Буфер нормалей (для освещения)
        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(normals)
                position(0)
            }
        }

        // Буфер индексов
        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indices)
                position(0)
            }
        }
    }

    // Генерация вершин сферы [citation:1]
    private fun generateVertices(): FloatArray {
        val vertices = mutableListOf<Float>()

        for (i in 0..stacks) {
            val theta = i.toFloat() / stacks * PI.toFloat() // от 0 до PI
            val sinTheta = sin(theta)
            val cosTheta = cos(theta)

            for (j in 0..slices) {
                val phi = j.toFloat() / slices * 2 * PI.toFloat() // от 0 до 2PI
                val sinPhi = sin(phi)
                val cosPhi = cos(phi)

                val x = cosPhi * sinTheta
                val y = cosTheta
                val z = sinPhi * sinTheta

                vertices.add(x * radius)
                vertices.add(y * radius)
                vertices.add(z * radius)
            }
        }

        return vertices.toFloatArray()
    }

    // Генерация нормалей (для освещения)
    private fun generateNormals(vertices: FloatArray): FloatArray {
        // Для сферы нормали совпадают с направлением из центра
        val normals = FloatArray(vertices.size)
        for (i in vertices.indices step 3) {
            val x = vertices[i]
            val y = vertices[i + 1]
            val z = vertices[i + 2]
            val length = kotlin.math.sqrt(x*x + y*y + z*z)
            normals[i] = x / length
            normals[i + 1] = y / length
            normals[i + 2] = z / length
        }
        return normals
    }

    // Генерация индексов для треугольников
    private fun generateIndices(): ShortArray {
        val indices = mutableListOf<Short>()

        for (i in 0 until stacks) {
            for (j in 0 until slices) {
                val first = (i * (slices + 1) + j).toShort()
                val second = (first + slices + 1).toShort()

                indices.add(first)
                indices.add(second)
                indices.add((first + 1).toShort())

                indices.add(second)
                indices.add((second + 1).toShort())
                indices.add((first + 1).toShort())
            }
        }

        return indices.toShortArray()
    }

    // Обновление углов (вызывается каждый кадр)
    fun update(deltaTime: Float) {
        orbitAngle += orbitSpeed * deltaTime
        if (orbitAngle > 360f) orbitAngle -= 360f

        rotationAngle += rotationSpeed * deltaTime
        if (rotationAngle > 360f) rotationAngle -= 360f

        if (isMoon) {
            moonOrbitAngle += orbitSpeed * deltaTime * 1.5f // Луна быстрее
            if (moonOrbitAngle > 360f) moonOrbitAngle -= 360f
        }
    }

    // Получение матрицы модели для планеты
    fun getModelMatrix(parentMatrix: FloatArray = FloatArray(16).also { android.opengl.Matrix.setIdentityM(it, 0) }): FloatArray {
        val modelMatrix = FloatArray(16)
        android.opengl.Matrix.setIdentityM(modelMatrix, 0)

        // Применяем родительскую матрицу (например, для Луны - матрицу Земли)
        android.opengl.Matrix.multiplyMM(modelMatrix, 0, parentMatrix, 0, modelMatrix, 0)

        if (isMoon) {
            // Для Луны: перпендикулярное вращение вокруг оси X
            android.opengl.Matrix.rotateM(modelMatrix, 0, moonOrbitAngle, 1f, 0f, 0f) // Вращение перпендикулярно эклиптике
            android.opengl.Matrix.translateM(modelMatrix, 0, orbitRadius, 0f, 0f)
        } else {
            // Для планет: вращение в плоскости эклиптики (вокруг Y)
            android.opengl.Matrix.rotateM(modelMatrix, 0, orbitAngle, 0f, 1f, 0f)
            android.opengl.Matrix.translateM(modelMatrix, 0, orbitRadius, 0f, 0f)
        }

        // Собственное вращение планеты вокруг оси
        android.opengl.Matrix.rotateM(modelMatrix, 0, rotationAngle, 0f, 1f, 0f)

        return modelMatrix
    }

    // Отрисовка планеты
    fun draw(program: Int, mvpMatrixHandle: Int, positionHandle: Int, normalHandle: Int, colorUniformHandle: Int) {
        // Вершины
        vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        // Нормали (для освещения)
        normalBuffer.position(0)
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, normalBuffer)

        // Цвет
        GLES20.glUniform4fv(colorUniformHandle, 1, color, 0)

        // Отрисовка
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        // Отключение атрибутов
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
    }
}
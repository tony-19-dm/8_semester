package com.example.astronomy.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class Square {

    // Вершины квадрата (два треугольника)
    private val vertices = floatArrayOf(
        // Передняя грань
        -1.0f, -1.0f, 0.0f,  // нижний левый
        1.0f, -1.0f, 0.0f,  // нижний правый
        -1.0f,  1.0f, 0.0f,  // верхний левый
        1.0f,  1.0f, 0.0f   // верхний правый
    )

    // Координаты текстуры
    private val textureCoords = floatArrayOf(
        0.0f, 1.0f,  // нижний левый
        1.0f, 1.0f,  // нижний правый
        0.0f, 0.0f,  // верхний левый
        1.0f, 0.0f   // верхний правый
    )

    // Порядок отрисовки треугольников
    private val drawOrder = shortArrayOf(0, 1, 2, 2, 1, 3)

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private val drawListBuffer: ShortBuffer

    private val vertexShaderCode = """
        attribute vec4 vPosition;
        attribute vec2 aTexCoord;
        varying vec2 vTexCoord;
        uniform mat4 uMVPMatrix;
        void main() {
            gl_Position = uMVPMatrix * vPosition;
            vTexCoord = aTexCoord;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec2 vTexCoord;
        uniform sampler2D uTexture;
        void main() {
            gl_FragColor = texture2D(uTexture, vTexCoord);
        }
    """

    private val program: Int

    init {
        // Инициализация буферов вершин
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(vertices)
        vertexBuffer.position(0)

        // Инициализация буферов текстуры
        val tb = ByteBuffer.allocateDirect(textureCoords.size * 4)
        tb.order(ByteOrder.nativeOrder())
        textureBuffer = tb.asFloatBuffer()
        textureBuffer.put(textureCoords)
        textureBuffer.position(0)

        // Инициализация буфера порядка отрисовки
        val db = ByteBuffer.allocateDirect(drawOrder.size * 2)
        db.order(ByteOrder.nativeOrder())
        drawListBuffer = db.asShortBuffer()
        drawListBuffer.put(drawOrder)
        drawListBuffer.position(0)

        // Компиляция шейдеров
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        // Создание программы
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
    }

    fun draw(mvpMatrix: FloatArray, textureId: Int) {
        // Используем программу
        GLES20.glUseProgram(program)

        // Получаем ссылки на атрибуты
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, 3,
            GLES20.GL_FLOAT, false,
            3 * 4, vertexBuffer
        )

        // Получаем ссылки на координаты текстуры
        val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(
            texCoordHandle, 2,
            GLES20.GL_FLOAT, false,
            2 * 4, textureBuffer
        )

        // Передаем матрицу преобразования
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Активируем текстуру
        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        // Рисуем квадрат
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES, drawOrder.size,
            GLES20.GL_UNSIGNED_SHORT, drawListBuffer
        )

        // Отключаем массивы вершин
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }
}
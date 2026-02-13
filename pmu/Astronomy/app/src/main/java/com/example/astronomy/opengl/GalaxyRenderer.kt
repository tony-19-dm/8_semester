package com.example.astronomy.opengl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import com.example.astronomy.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GalaxyRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var square: Square
    private lateinit var cube: Cube

    // Шейдерная программа
    private var program = 0

    // Хендлы (указатели на переменные в шейдерах)
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var mvpMatrixHandle = 0
    private var textureHandle = 0

    // ID текстуры
    private var textureId = 0

    // Матрицы
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrixForCube = FloatArray(16)
    private val modelMatrixForSquare = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // Вращение куба
    private var rotationAngle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Серый фон (не влияет, т.к. квадрат закроет всё)
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f)

        square = Square()
        cube = Cube()

        // Создаем программу
        program = createProgram()
        GLES20.glUseProgram(program)

        // Получаем хендлы
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        textureHandle = GLES20.glGetUniformLocation(program, "uTexture")

        // Загружаем текстуру
        textureId = loadTexture(context, R.drawable.galaxy) // убедитесь, что файл есть!

        // Включаем тест глубины (чтобы куб не смешивался с фоном неправильно)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height

        // Проекционная матрица: перспектива
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 20f)

        // Видовая матрица: камера стоит в (0,0,5), смотрит в (0,0,0)
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 5f,   // позиция камеры
            0f, 0f, 0f,   // точка взгляда
            0f, 1f, 0f)   // up-вектор
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // ---- 1. Рисуем КВАДРАТ (ФОН) ----
        // Отодвигаем назад и увеличиваем
        Matrix.setIdentityM(modelMatrixForSquare, 0)
        Matrix.translateM(modelMatrixForSquare, 0, 0f, 0f, -8f) // далеко назад
        Matrix.scaleM(modelMatrixForSquare, 0, 5f, 5f, 1f)      // растягиваем

        // Комбинируем матрицы
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrixForSquare, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        // Передаем матрицу в шейдер
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Рисуем квадрат
        drawSquare()

        // ---- 2. Рисуем КУБ (по центру) ----
        Matrix.setIdentityM(modelMatrixForCube, 0)
        // Легкое вращение для красоты (необязательно, но выглядит круто)
        rotationAngle += 0.5f
        Matrix.rotateM(modelMatrixForCube, 0, rotationAngle, 0.5f, 1f, 0.3f)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrixForCube, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Рисуем куб
        drawCube()
    }

    private fun drawSquare() {
        // Используем текстуру
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureHandle, 0)

        // Вершинный буфер (позиция: 3 компонента, шаг 5*4 = 20 байт)
        square.vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, square.vertexBuffer)

        // Текстурные координаты (2 компонента, шаг 5*4, смещение 3*4 = 12 байт)
        square.vertexBuffer.position(3)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 5 * 4, square.vertexBuffer)

        // Рисуем
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, square.indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, square.indexBuffer)

        // Отключаем атрибуты
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun drawCube() {
        // 1. Вершинный буфер (позиции)
        cube.vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 3 * 4, cube.vertexBuffer)

        // 2. Текстурные координаты-заглушка (8 вершин * 2 компонента = 16 float-ов)
        val texCoords = FloatArray(16) // все нули — шейдеру всё равно
        val texBuffer = ByteBuffer.allocateDirect(texCoords.size * 4) // 16 * 4 = 64 байта
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        texBuffer.put(texCoords)
        texBuffer.position(0)

        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer)

        // 3. Отрисовка куба
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, cube.indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, cube.indexBuffer)

        // 4. Отключение атрибутов
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    // ---------- Утилиты: компиляция шейдеров и загрузка текстуры ----------
    private fun createProgram(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, ShaderHelper.vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, ShaderHelper.fragmentShaderCode)

        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    private fun loadTexture(context: Context, resourceId: Int): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0])

        // Настройки текстуры
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)

        // Загружаем битмап
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

        if (bitmap == null) {
            // Если текстура не найдена — создаем шахматную доску (чтобы не крашилось)
            val chessboard = createChessboardBitmap()
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, chessboard, 0)
            chessboard.recycle()
        } else {
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
            bitmap.recycle()
        }

        return texture[0]
    }

    private fun createChessboardBitmap(): Bitmap {
        val size = 256
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                val isBlack = ((x / 32) + (y / 32)) % 2 == 0
                bitmap.setPixel(x, y, if (isBlack) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }
}
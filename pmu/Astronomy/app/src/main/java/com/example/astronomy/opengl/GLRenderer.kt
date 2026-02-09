package com.example.astronomy.opengl

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var square: Square
    private lateinit var cube: Cube
    private var textureId: Int = 0

    // Матрицы для преобразований
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrixSquare = FloatArray(16)
    private val modelMatrixCube = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private var rotationAngle: Float = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Устанавливаем цвет фона (черный)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)

        // Включаем проверку глубины
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Инициализируем фигуры
        square = Square()
        cube = Cube()

        // Загружаем текстуру (создадим потом метод)
        textureId = TextureHelper.loadTexture(context, R.drawable.galaxy)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        // Устанавливаем область просмотра
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height.toFloat()

        // Создаем перспективную проекцию
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 10f)

        // Настраиваем камеру
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 0f, 5f,    // позиция камеры
            0f, 0f, 0f,    // точка, куда смотрим
            0f, 1f, 0f     // вектор "вверх"
        )
    }

    override fun onDrawFrame(gl: GL10?) {
        // Очищаем экран и буфер глубины
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        rotationAngle += 0.5f

        // Рисуем квадрат (фон)
        drawSquare()

        // Рисуем куб
        drawCube()
    }

    private fun drawSquare() {
        Matrix.setIdentityM(modelMatrixSquare, 0)

        // Отодвигаем квадрат назад
        Matrix.translateM(modelMatrixSquare, 0, 0f, 0f, -7f)

        // Масштабируем квадрат, чтобы он был фоном
        Matrix.scaleM(modelMatrixSquare, 0, 10f, 10f, 1f)

        // Вычисляем итоговую матрицу
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrixSquare, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        // Рисуем квадрат
        square.draw(mvpMatrix, textureId)
    }

    private fun drawCube() {
        Matrix.setIdentityM(modelMatrixCube, 0)

        // Размещаем куб по центру
        Matrix.translateM(modelMatrixCube, 0, 0f, 0f, 0f)

        // Вращаем куб
        Matrix.rotateM(modelMatrixCube, 0, rotationAngle, 1f, 1f, 1f)

        // Масштабируем куб
        Matrix.scaleM(modelMatrixCube, 0, 0.5f, 0.5f, 0.5f)

        // Вычисляем итоговую матрицу
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrixCube, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        // Рисуем куб
        cube.draw(mvpMatrix)
    }
}
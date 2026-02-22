package com.example.astronomy.Planets

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.astronomy.opengl.ShaderHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SolarSystemRenderer(private val context: Context) : GLSurfaceView.Renderer {

    // Планеты
    private lateinit var sun: Planet
    private lateinit var earth: Planet
    private lateinit var moon: Planet
    private lateinit var mars: Planet
    private lateinit var venus: Planet
    private lateinit var jupiter: Planet

    // Шейдерная программа
    private var program = 0

    // Хендлы
    private var positionHandle = 0
    private var normalHandle = 0
    private var mvpMatrixHandle = 0
    private var modelMatrixHandle = 0
    private var colorHandle = 0
    private var lightPositionHandle = 0

    // Матрицы
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // Позиция источника света (Солнце)
    private val lightPosition = floatArrayOf(0f, 0f, 0f)

    // Время для анимации
    private var previousTime = System.currentTimeMillis()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Создаем программу
        program = createProgram()
        GLES20.glUseProgram(program)

        // Получаем хендлы
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        normalHandle = GLES20.glGetAttribLocation(program, "aNormal")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        modelMatrixHandle = GLES20.glGetUniformLocation(program, "uModelMatrix")
        colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        lightPositionHandle = GLES20.glGetUniformLocation(program, "uLightPosition")

        // Инициализируем планеты
        initPlanets()
    }

    private fun initPlanets() {
        // Масштаб: 1 единица = 1 миллион миль (примерно) [citation:7]
        // Радиус Солнца: 0.4, Земли: 0.04 (увеличено для видимости)

        sun = Planet(0.4f, 48, 48, floatArrayOf(1.0f, 0.8f, 0.3f, 1.0f)) // Желтый

        earth = Planet(0.04f, 32, 32, floatArrayOf(0.2f, 0.6f, 1.0f, 1.0f)) // Голубой
        earth.orbitRadius = 1.5f
        earth.orbitSpeed = 10f      // градусов в секунду
        earth.rotationSpeed = 360f  // полный оборот за 1 секунду

        moon = Planet(0.01f, 16, 16, floatArrayOf(0.8f, 0.8f, 0.8f, 1.0f)) // Серый
        moon.orbitRadius = 0.2f
        moon.orbitSpeed = 40f       // быстрее Земли
        moon.rotationSpeed = 40f
        moon.isMoon = true          // специальный режим для Луны

        mars = Planet(0.03f, 32, 32, floatArrayOf(0.9f, 0.4f, 0.2f, 1.0f)) // Красный
        mars.orbitRadius = 2.0f
        mars.orbitSpeed = 8f
        mars.rotationSpeed = 350f

        venus = Planet(0.035f, 32, 32, floatArrayOf(0.9f, 0.7f, 0.4f, 1.0f)) // Желтоватый
        venus.orbitRadius = 1.2f
        venus.orbitSpeed = 12f
        venus.rotationSpeed = 200f

        jupiter = Planet(0.08f, 48, 48, floatArrayOf(0.8f, 0.6f, 0.4f, 1.0f)) // Коричневатый
        jupiter.orbitRadius = 3.0f
        jupiter.orbitSpeed = 3f
        jupiter.rotationSpeed = 400f
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height

        // Проекционная матрица
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 20f)

        // Видовая матрица (камера)
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 3f, 8f,   // позиция камеры (смещена вверх для обзора)
            0f, 0f, 0f,   // точка взгляда
            0f, 1f, 0f)   // up-вектор
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Вычисляем deltaTime для плавной анимации
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - previousTime) / 1000f
        previousTime = currentTime

        // Обновляем позиции планет
        updatePlanets(deltaTime)

        // Устанавливаем позицию света (Солнце в центре)
        GLES20.glUniform3f(lightPositionHandle, lightPosition[0], lightPosition[1], lightPosition[2])

        // Рисуем Солнце (неподвижно в центре)
        drawPlanet(sun, FloatArray(16).also { Matrix.setIdentityM(it, 0) })

        // Рисуем Венеру
        drawPlanet(venus, FloatArray(16).also { Matrix.setIdentityM(it, 0) })

        // Рисуем Землю
        val earthMatrix = drawPlanet(earth, FloatArray(16).also { Matrix.setIdentityM(it, 0) })

        // Рисуем Луну (используем матрицу Земли как родительскую)
        drawPlanet(moon, earthMatrix)

        // Рисуем Марс
        drawPlanet(mars, FloatArray(16).also { Matrix.setIdentityM(it, 0) })

        // Рисуем Юпитер
        drawPlanet(jupiter, FloatArray(16).also { Matrix.setIdentityM(it, 0) })
    }

    private fun updatePlanets(deltaTime: Float) {
        sun.update(deltaTime)
        earth.update(deltaTime)
        moon.update(deltaTime)
        mars.update(deltaTime)
        venus.update(deltaTime)
        jupiter.update(deltaTime)
    }

    private fun drawPlanet(planet: Planet, parentMatrix: FloatArray): FloatArray {
        // Получаем матрицу модели для планеты
        val modelMatrix = planet.getModelMatrix(parentMatrix)

        // Вычисляем матрицу MVP
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        // Передаем матрицы в шейдер
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0)

        // Рисуем планету
        planet.draw(program, mvpMatrixHandle, positionHandle, normalHandle, colorHandle)

        return modelMatrix
    }

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
}
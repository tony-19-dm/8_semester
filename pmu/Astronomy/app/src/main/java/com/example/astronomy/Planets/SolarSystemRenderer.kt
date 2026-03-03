package com.example.astronomy.Planets

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.astronomy.MoonDetailActivity
import com.example.astronomy.R
import com.example.astronomy.opengl.SelectionCube
import com.example.astronomy.opengl.ShaderHelper
import com.example.astronomy.opengl.Square
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SolarSystemRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var backgroundSquare: Square
    private var backgroundTextureId = 0
    private var backgroundProgram = 0
    private var bgPositionHandle = 0
    private var bgTexCoordHandle = 0
    private var bgMVPMatrixHandle = 0

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

    private var emissiveHandle = 0
    // Матрицы
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // Позиция источника света (Солнце)
    private val lightPosition = floatArrayOf(0f, 0f, 0f)

    // Время для анимации
    private var previousTime = System.currentTimeMillis()

    private lateinit var selectionCube: SelectionCube
    private var cubeProgram = 0
    private var cubePositionHandle = 0
    private var cubeColorHandle = 0
    private var cubeMVPMatrixHandle = 0

    // Список планет для выбора
    private val planetsList = mutableListOf<Planet>()
    private var selectedPlanetIndex = 0

    // Матрица для куба
    private val cubeModelMatrix = FloatArray(16)

    private var cubeRotationAngle = 0f

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Создаем программу
        program = createProgram()
        GLES20.glUseProgram(program)

        backgroundSquare = Square()
        backgroundTextureId = loadTexture(R.drawable.galaxy)

        // Создаём программу для фона
        backgroundProgram = createBackgroundProgram()

        bgPositionHandle = GLES20.glGetAttribLocation(backgroundProgram, "aPosition")
        bgTexCoordHandle = GLES20.glGetAttribLocation(backgroundProgram, "aTexCoord")
        bgMVPMatrixHandle = GLES20.glGetUniformLocation(backgroundProgram, "uMVPMatrix")

        // Получаем хендлы
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        normalHandle = GLES20.glGetAttribLocation(program, "aNormal")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        modelMatrixHandle = GLES20.glGetUniformLocation(program, "uModelMatrix")
        colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        lightPositionHandle = GLES20.glGetUniformLocation(program, "uLightPosition")

        emissiveHandle = GLES20.glGetUniformLocation(program, "uEmissive")

        // Инициализируем планеты
        initPlanets()

        // Создаём куб выбора
        selectionCube = SelectionCube()

// Создаём программу для куба (простой шейдер с цветом)
        cubeProgram = createCubeProgram()
        cubePositionHandle = GLES20.glGetAttribLocation(cubeProgram, "aPosition")
//        cubeColorHandle = GLES20.glGetAttribLocation(cubeProgram, "aColor")
        cubeMVPMatrixHandle = GLES20.glGetUniformLocation(cubeProgram, "uMVPMatrix")
    }

    private fun createBackgroundProgram(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, ShaderHelper.bgVertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, ShaderHelper.bgFragmentShaderCode)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }

    private fun loadTexture(resourceId: Int): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        val options = android.graphics.BitmapFactory.Options()
        options.inScaled = false
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, resourceId, options)

        if (bitmap == null) {
            android.util.Log.e("SolarSystemRenderer", "Failed to load texture: resourceId=$resourceId")
            return 0
        }
        android.util.Log.d("SolarSystemRenderer", "Texture loaded: ${bitmap.width}x${bitmap.height}, id=${textureIds[0]}")

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

        // Настройки текстуры
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        return textureIds[0]
    }

    private fun drawBackground() {
        // Используем программу для фона
        GLES20.glUseProgram(backgroundProgram)

        // Матрица для фона: отодвинуть назад и увеличить
        val bgModelMatrix = FloatArray(16)
        android.opengl.Matrix.setIdentityM(bgModelMatrix, 0)
        android.opengl.Matrix.translateM(bgModelMatrix, 0, 0f, -5f, -10f)  // далеко назад
        android.opengl.Matrix.scaleM(bgModelMatrix, 0, 12f, 12f, 1f)      // растянуть

        // MVP матрица
        val mvpTemp = FloatArray(16)
        android.opengl.Matrix.multiplyMM(mvpTemp, 0, viewMatrix, 0, bgModelMatrix, 0)
        android.opengl.Matrix.multiplyMM(mvpTemp, 0, projectionMatrix, 0, mvpTemp, 0)
        GLES20.glUniformMatrix4fv(bgMVPMatrixHandle, 1, false, mvpTemp, 0)

        // Текстура
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, backgroundTextureId)
        val textureHandle = GLES20.glGetUniformLocation(backgroundProgram, "uTexture")
        GLES20.glUniform1i(textureHandle, 0)

        // Вершинный буфер (позиция)
        backgroundSquare.vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(bgPositionHandle)
        GLES20.glVertexAttribPointer(bgPositionHandle, 3, GLES20.GL_FLOAT, false, 5 * 4, backgroundSquare.vertexBuffer)

        // Текстурные координаты
        backgroundSquare.vertexBuffer.position(3)
        GLES20.glEnableVertexAttribArray(bgTexCoordHandle)
        GLES20.glVertexAttribPointer(bgTexCoordHandle, 2, GLES20.GL_FLOAT, false, 5 * 4, backgroundSquare.vertexBuffer)

        // Рисуем
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, backgroundSquare.indexBuffer.capacity(), GLES20.GL_UNSIGNED_SHORT, backgroundSquare.indexBuffer)

        // Отключаем атрибуты
        GLES20.glDisableVertexAttribArray(bgPositionHandle)
        GLES20.glDisableVertexAttribArray(bgTexCoordHandle)

        // Возвращаем программу планет
        GLES20.glUseProgram(program)
    }

    private fun initPlanets() {

        sun = Planet(0.4f, 48, 48, floatArrayOf(1.0f, 1.0f, 0.0f, 0.0f)) // Желтый

        earth = Planet(0.15f, 32, 32, floatArrayOf(0.2f, 0.6f, 1.0f, 1.0f)) // Голубой
        earth.orbitRadius = 2.0f
        earth.orbitSpeed = 10f      // градусов в секунду
        earth.rotationSpeed = 360f  // полный оборот за 1 секунду

        moon = Planet(0.05f, 16, 16, floatArrayOf(0.8f, 0.8f, 0.8f, 1.0f)) // Серый
        moon.orbitRadius = 0.5f
        moon.orbitSpeed = 40f
        moon.rotationSpeed = 0f
        moon.isMoon = true          // режим Луны

        mars = Planet(0.12f, 32, 32, floatArrayOf(0.9f, 0.4f, 0.2f, 1.0f)) // Красный
        mars.orbitRadius = 2.8f
        mars.orbitSpeed = 8f
        mars.rotationSpeed = 350f

        venus = Planet(0.13f, 32, 32, floatArrayOf(0.9f, 0.7f, 0.4f, 1.0f)) // Желтоватый
        venus.orbitRadius = 1.2f
        venus.orbitSpeed = 12f
        venus.rotationSpeed = 200f

        jupiter = Planet(0.25f, 48, 48, floatArrayOf(0.8f, 0.6f, 0.4f, 1.0f)) // Коричневатый
        jupiter.orbitRadius = 4.0f
        jupiter.orbitSpeed = 3f
        jupiter.rotationSpeed = 400f

        sun.name = "Солнце"
        earth.name = "Земля"
        moon.name = "Луна"
        mars.name = "Марс"
        venus.name = "Венера"
        jupiter.name = "Юпитер"

        planetsList.clear()
        planetsList.add(sun)
        planetsList.add(venus)
        planetsList.add(earth)
        planetsList.add(moon)
        planetsList.add(mars)
        planetsList.add(jupiter)
    }

    fun selectNextPlanet() {
        selectedPlanetIndex = (selectedPlanetIndex + 1) % planetsList.size
    }

    fun selectPreviousPlanet() {
        selectedPlanetIndex = (selectedPlanetIndex - 1 + planetsList.size) % planetsList.size
    }

    fun showPlanetInfo(activity: Activity) {
        val selectedPlanet = planetsList[selectedPlanetIndex]
        if (selectedPlanet === moon) {
            val intent = Intent(activity, MoonDetailActivity::class.java)
            activity.startActivity(intent)
        } else {
            android.widget.Toast.makeText(activity,
                "Выбрана: ${selectedPlanet.name}",
                android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawSelectionCube() {
        val selectedPlanet = planetsList[selectedPlanetIndex]

        // Получаем мировую позицию выбранной планеты
        val planetPos: FloatArray

        if (selectedPlanet === moon) {
            val earthMatrix = earth.getModelMatrix(FloatArray(16).also { Matrix.setIdentityM(it, 0) })
            planetPos = moon.getWorldPosition(earthMatrix)
        } else {
            planetPos = selectedPlanet.getWorldPosition()
        }

        // Матрица для куба
        Matrix.setIdentityM(cubeModelMatrix, 0)
        Matrix.translateM(cubeModelMatrix, 0, planetPos[0], planetPos[1], planetPos[2])

        // Масштабируем под размер планеты
        val scale = selectedPlanet.radius * 1.8f
        Matrix.scaleM(cubeModelMatrix, 0, scale, scale, scale)

        Matrix.rotateM(cubeModelMatrix, 0, cubeRotationAngle, 0.7f, 0.7f, 0.7f) // равномерное вращение

        // Вычисляем MVP
        val mvpTemp = FloatArray(16)
        Matrix.multiplyMM(mvpTemp, 0, viewMatrix, 0, cubeModelMatrix, 0)
        Matrix.multiplyMM(mvpTemp, 0, projectionMatrix, 0, mvpTemp, 0)

        // Рисуем куб
        GLES20.glUseProgram(cubeProgram)
        GLES20.glUniformMatrix4fv(cubeMVPMatrixHandle, 1, false, mvpTemp, 0)

        // Получаем хендл для цвета
        val colorUniform = GLES20.glGetUniformLocation(cubeProgram, "uColor")

        // Рисуем куб
        selectionCube.draw(cubeProgram, cubeMVPMatrixHandle, cubePositionHandle, colorUniform)

        GLES20.glUseProgram(program)
    }

    private fun createCubeProgram(): Int {
        val vertexShaderCode = """
        attribute vec4 aPosition;
        uniform mat4 uMVPMatrix;
        void main() {
            gl_Position = uMVPMatrix * aPosition;
        }
    """.trimIndent()

        val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 uColor;
        void main() {
            gl_FragColor = uColor;
        }
    """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)
        return program
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height

        // Проекционная матрица
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 30f)

        // Видовая матрица (камера)
        Matrix.setLookAtM(viewMatrix, 0,
            0f, 5f, 12f,
            0f, 0f, 0f,
            0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        drawBackground()

        // Вычисляем deltaTime для плавной анимации
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - previousTime) / 1000f
        previousTime = currentTime

        // Обновляем позиции планет
        updatePlanets(deltaTime)

        // Устанавливаем позицию света (Солнце в центре)
        GLES20.glUniform3f(lightPositionHandle, lightPosition[0], lightPosition[1], lightPosition[2])

        // Рисуем Солнце
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

        // Обновляем угол вращения куба
        cubeRotationAngle += deltaTime * 100f  // 100 градусов в секунду
        if (cubeRotationAngle > 360f) cubeRotationAngle -= 360f

        drawSelectionCube()
    }

    private fun updatePlanets(deltaTime: Float) {
        sun.update(deltaTime)
        venus.update(deltaTime)
        earth.update(deltaTime)
        // Луну обновляем отдельно, но она сама обновит свои углы
        moon.update(deltaTime)
        mars.update(deltaTime)
        jupiter.update(deltaTime)
    }

    private fun drawPlanet(planet: Planet, parentMatrix: FloatArray): FloatArray {
        // Получаем матрицу модели для планеты
        val modelMatrix = planet.getModelMatrix(parentMatrix)

        // Вычисляем матрицу MVP
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        if (planet === sun) {
            GLES20.glUniform1f(emissiveHandle, 1.0f)
        } else {
            GLES20.glUniform1f(emissiveHandle, 0.0f)
        }

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
package com.example.astronomy

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import com.example.astronomy.Planets.Planet
import com.example.astronomy.utils.WaterTextureGenerator
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class NeptuneRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var neptune: Planet
    private var textureProgram = 0
    private var rotationAngle = 0f

    // Хендлы шейдера
    private var positionHandle = 0
    private var normalHandle = 0
    private var texCoordHandle = 0
    private var mvpMatrixHandle = 0
    private var modelMatrixHandle = 0
    private var lightPositionHandle = 0
    private var textureUniformHandle = 0

    // Матрицы
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    // Позиция источника света
    private val lightPosition = floatArrayOf(2f, 2f, 3f)

    // Текстура
    private var textureId = 0
    private var time = 0f

    // Вершинный шейдер с текстурными координатами
    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform mat4 uModelMatrix;
        attribute vec4 aPosition;
        attribute vec3 aNormal;
        attribute vec2 aTexCoord;
        varying vec3 vNormal;
        varying vec3 vPosition;
        varying vec2 vTexCoord;
        
        void main() {
            gl_Position = uMVPMatrix * aPosition;
            vPosition = vec3(uModelMatrix * aPosition);
            vNormal = vec3(uModelMatrix * vec4(aNormal, 0.0));
            vTexCoord = aTexCoord;
        }
    """.trimIndent()

    // Фрагментный шейдер с текстурой и освещением
    private val fragmentShaderCode = """
        precision mediump float;
        uniform vec3 uLightPosition;
        uniform sampler2D uTexture;
        varying vec3 vNormal;
        varying vec3 vPosition;
        varying vec2 vTexCoord;
        
        void main() {
            vec3 normal = normalize(vNormal);
            vec3 lightDir = normalize(uLightPosition - vPosition);
            vec3 viewDir = vec3(0.0, 0.0, 1.0);
            
            // Ambient
            float ambient = 0.3;
            
            // Diffuse
            float diffuse = max(dot(normal, lightDir), 0.0);
            
            // Specular
            vec3 reflectDir = reflect(-lightDir, normal);
            float specular = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
            
            // Цвет из текстуры
            vec4 texColor = texture2D(uTexture, vTexCoord);
            
            vec3 finalColor = texColor.rgb * (ambient + diffuse + specular * 0.5);
            gl_FragColor = vec4(finalColor, 1.0);
        }
    """.trimIndent()

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Создаём программу
        textureProgram = createProgram()
        GLES20.glUseProgram(textureProgram)

        // Получаем хендлы
        positionHandle = GLES20.glGetAttribLocation(textureProgram, "aPosition")
        normalHandle = GLES20.glGetAttribLocation(textureProgram, "aNormal")
        texCoordHandle = GLES20.glGetAttribLocation(textureProgram, "aTexCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(textureProgram, "uMVPMatrix")
        modelMatrixHandle = GLES20.glGetUniformLocation(textureProgram, "uModelMatrix")
        lightPositionHandle = GLES20.glGetUniformLocation(textureProgram, "uLightPosition")
        textureUniformHandle = GLES20.glGetUniformLocation(textureProgram, "uTexture")

        // Создаём планету Нептун (увеличенную для детального просмотра)
        neptune = Planet(0.8f, 64, 64, floatArrayOf(0.0f, 0.0f, 0.8f, 1.0f))

        // Создаём текстуру
        textureId = createTexture()
        updateTexture()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val ratio = width.toFloat() / height
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 1f, 20f)

        Matrix.setLookAtM(viewMatrix, 0,
            0f, 1f, 3f,  // камера ближе
            0f, 0f, 0f,
            0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Обновляем время и текстуру
        time += 0.05f
        if (time > 2f * Math.PI.toFloat()) {
            time -= 2f * Math.PI.toFloat()
        }
        updateTexture()

        // Вращаем планету
        rotationAngle += 0.5f
        if (rotationAngle > 360f) rotationAngle -= 360f

        // Создаём матрицу модели
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, rotationAngle, 0f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, 30f, 1f, 0f, 0f) // небольшой наклон

        // Вычисляем MVP
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        // Передаём матрицы в шейдер
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0)

        // Позиция света
        GLES20.glUniform3f(lightPositionHandle, lightPosition[0], lightPosition[1], lightPosition[2])

        // Биндим текстуру
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(textureUniformHandle, 0)

        // Рисуем планету с текстурой
        drawPlanetWithTexture()
    }

    private fun drawPlanetWithTexture() {
        // Позиции вершин
        neptune.vertexBuffer.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, neptune.vertexBuffer)

        // Нормали
        neptune.normalBuffer.position(0)
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, neptune.normalBuffer)

        // Генерируем текстурные координаты
        val texCoords = generateTexCoords()
        val texCoordBuffer = java.nio.ByteBuffer.allocateDirect(texCoords.size * 4).run {
            order(java.nio.ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(texCoords)
                position(0)
            }
        }

        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)

        // Рисуем
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, neptune.indexBuffer.capacity(),
            GLES20.GL_UNSIGNED_SHORT, neptune.indexBuffer)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun generateTexCoords(): FloatArray {
        val stacks = 64
        val slices = 64
        val texCoords = FloatArray((stacks + 1) * (slices + 1) * 2)
        var index = 0
        for (i in 0..stacks) {
            for (j in 0..slices) {
                texCoords[index++] = j.toFloat() / slices
                texCoords[index++] = i.toFloat() / stacks
            }
        }
        return texCoords
    }

    private fun createTexture(): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)

        return textureIds[0]
    }

    private fun updateTexture() {
        val waterBitmap = WaterTextureGenerator.generateWaterTexture(512, 512, time)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, waterBitmap, 0)
        waterBitmap.recycle()
    }

    private fun createProgram(): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
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
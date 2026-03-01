package com.example.astronomy.Planets

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.astronomy.R
import com.example.astronomy.opengl.ShaderHelper
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MoonRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var moon: Planet
    private var program = 0

    private var positionHandle = 0
    private var normalHandle = 0
    private var mvpMatrixHandle = 0
    private var modelMatrixHandle = 0
    private var colorHandle = 0
    private var lightPositionHandle = 0
    private var emissiveHandle = 0

    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val mvpMatrix = FloatArray(16)

    private val lightPosition = floatArrayOf(5f, 5f, 5f) // свет сбоку для красивых теней

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        program = createProgram()
        GLES20.glUseProgram(program)

        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        normalHandle = GLES20.glGetAttribLocation(program, "aNormal")
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        modelMatrixHandle = GLES20.glGetUniformLocation(program, "uModelMatrix")
        colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        lightPositionHandle = GLES20.glGetUniformLocation(program, "uLightPosition")
        emissiveHandle = GLES20.glGetUniformLocation(program, "uEmissive")

        // Создаём Луну крупным планом
        moon = Planet(0.8f, 64, 64, floatArrayOf(0.9f, 0.9f, 0.9f, 1.0f))
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        Matrix.perspectiveM(projectionMatrix, 0, 45f, ratio, 1f, 20f)

        Matrix.setLookAtM(viewMatrix, 0,
            0f, 2f, 4f,
            0f, 0f, 0f,
            0f, 1f, 0f)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUniform3f(lightPositionHandle, lightPosition[0], lightPosition[1], lightPosition[2])
        GLES20.glUniform1f(emissiveHandle, 0.0f)

        // Вращаем Луну для красоты
        val modelMatrix = FloatArray(16)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.rotateM(modelMatrix, 0, System.currentTimeMillis() * 0.05f, 0f, 1f, 0f)

        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(modelMatrixHandle, 1, false, modelMatrix, 0)

        moon.draw(program, mvpMatrixHandle, positionHandle, normalHandle, colorHandle)
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
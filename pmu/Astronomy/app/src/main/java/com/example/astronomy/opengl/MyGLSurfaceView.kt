package com.example.astronomy.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class MyGLSurfaceView(context: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs) {

    private val renderer: GLRenderer

    init {
        // Устанавливаем версию OpenGL ES 2.0
        setEGLContextClientVersion(2)

        // Создаем рендерер
        renderer = GLRenderer(context)
        setRenderer(renderer)

        // Режим отрисовки - непрерывный
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }
}
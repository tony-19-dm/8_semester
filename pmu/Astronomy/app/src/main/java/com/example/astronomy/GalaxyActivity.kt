package com.example.astronomy

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import com.example.astronomy.opengl.GalaxyRenderer

class GalaxyActivity : Activity() {

    private lateinit var glSurfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Устанавливаем layout с FrameLayout
        setContentView(R.layout.activity_galaxy)

        // Создаём GLSurfaceView
        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(GalaxyRenderer(this@GalaxyActivity))
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        // Добавляем GLSurfaceView в контейнер
        val container = findViewById<FrameLayout>(R.id.gl_container)
        container.addView(glSurfaceView)

        // Настраиваем кнопку "Назад"
        val backButton = findViewById<Button>(R.id.btn_back)
        backButton.setOnClickListener {
            finish() // закрывает текущую Activity и возвращает к предыдущей
        }
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
}
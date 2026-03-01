package com.example.astronomy

import android.app.Activity
import android.content.Intent
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import com.example.astronomy.Planets.SolarSystemRenderer

class GalaxyActivity : Activity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var renderer: SolarSystemRenderer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galaxy)

        // Создаём рендерер
        renderer = SolarSystemRenderer(this)

        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        val container = findViewById<FrameLayout>(R.id.gl_container)
        container.addView(glSurfaceView)

        // Кнопка назад
        findViewById<Button>(R.id.btn_back).setOnClickListener {
            finish()
        }

        // Кнопки управления выбором планет
        findViewById<Button>(R.id.btn_prev).setOnClickListener {
            renderer.selectPreviousPlanet()
        }

        findViewById<Button>(R.id.btn_next).setOnClickListener {
            renderer.selectNextPlanet()
        }

        findViewById<Button>(R.id.btn_info).setOnClickListener {
            renderer.showPlanetInfo(this)
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
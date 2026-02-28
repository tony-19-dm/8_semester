package com.example.astronomy

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import com.example.astronomy.Planets.SolarSystemRenderer

class GalaxyActivity : Activity() {

    private lateinit var glSurfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_galaxy)

        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(SolarSystemRenderer(this@GalaxyActivity))  // используем новый рендерер
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        val container = findViewById<FrameLayout>(R.id.gl_container)
        container.addView(glSurfaceView)

        val backButton = findViewById<Button>(R.id.btn_back)
        backButton.setOnClickListener {
            finish()
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
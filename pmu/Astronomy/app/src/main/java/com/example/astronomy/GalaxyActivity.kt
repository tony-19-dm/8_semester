package com.example.astronomy

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import com.example.astronomy.opengl.GalaxyRenderer

class GalaxyActivity : Activity() {  // ← наследуемся от Activity
    private lateinit var glSurfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(GalaxyRenderer(this@GalaxyActivity))
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        setContentView(glSurfaceView)
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
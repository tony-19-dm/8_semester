package com.example.astronomy

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout

class NeptuneDetailActivity : Activity() {

    private lateinit var glSurfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_neptune_detail)

        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(NeptuneRenderer(this@NeptuneDetailActivity))
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        val container = findViewById<FrameLayout>(R.id.gl_container_neptune)
        container.addView(glSurfaceView)

        findViewById<Button>(R.id.btn_back_neptune).setOnClickListener {
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
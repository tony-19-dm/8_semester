package com.example.astronomy

import android.app.Activity  // ← меняем импорт
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import com.example.astronomy.Planets.MoonRenderer

class MoonDetailActivity : Activity() {  // ← наследуемся от Activity, а не AppCompatActivity

    private lateinit var glSurfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_moon_detail)

        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(MoonRenderer(this@MoonDetailActivity))
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        val container = findViewById<FrameLayout>(R.id.gl_container_moon)
        container.addView(glSurfaceView)

        findViewById<Button>(R.id.btn_back_moon).setOnClickListener {
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
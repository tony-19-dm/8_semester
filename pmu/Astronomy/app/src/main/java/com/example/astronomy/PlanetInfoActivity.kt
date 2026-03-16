package com.example.astronomy

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.astronomy.data.PlanetsData
import com.example.astronomy.utils.WaterTextureGenerator

class PlanetInfoActivity : Activity() {

    private lateinit var imageView: ImageView
    private var animationHandler = Handler(Looper.getMainLooper())
    private var animationRunnable: Runnable? = null
    private var time = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planet_info)

        // Получаем название планеты из Intent
        val planetName = intent.getStringExtra("planet_name") ?: "Земля"

        // Получаем информацию о планете
        val planetInfo = PlanetsData.getInfo(planetName)

        // Находим элементы UI
        val nameView = findViewById<TextView>(R.id.planet_name)
        val descriptionView = findViewById<TextView>(R.id.planet_description)
        imageView = findViewById<ImageView>(R.id.planet_image)
        val backButton = findViewById<Button>(R.id.btn_back)

        // Заполняем данные
        if (planetInfo != null) {
            nameView.text = planetInfo.name
            descriptionView.text = planetInfo.description

            // Особый случай для Нептуна - генерируем водную текстуру
            if (planetName == "Нептун") {
                // Генерируем начальную текстуру
                val waterTexture = WaterTextureGenerator.generateWaterTexture(512, 512, time)
                imageView.setImageBitmap(waterTexture)

                // Запускаем анимацию волн
                startWaterAnimation()
            } else {
                // Для остальных планет используем статичное изображение
                imageView.setImageResource(planetInfo.imageResId)
            }

            // Устанавливаем цвет фона
            window.decorView.setBackgroundColor(planetInfo.color)
        } else {
            nameView.text = planetName
            descriptionView.text = "Информация о планете временно отсутствует."
        }

        // Кнопка назад
        backButton.setOnClickListener {
            stopWaterAnimation()
            finish()
        }
    }

    private fun startWaterAnimation() {
        animationRunnable = object : Runnable {
            override fun run() {
                // Увеличиваем время для анимации
                time += 0.1f

                // Генерируем новый кадр водной текстуры
                val waterTexture = WaterTextureGenerator.generateWaterTexture(512, 512, time)
                imageView.setImageBitmap(waterTexture)

                // Планируем следующий кадр через 50 мс (~20 FPS)
                animationHandler.postDelayed(this, 50)
            }
        }
        animationRunnable?.run()
    }

    private fun stopWaterAnimation() {
        animationHandler.removeCallbacksAndMessages(null)
        animationRunnable = null
    }

    override fun onPause() {
        super.onPause()
        stopWaterAnimation()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopWaterAnimation()
    }
}
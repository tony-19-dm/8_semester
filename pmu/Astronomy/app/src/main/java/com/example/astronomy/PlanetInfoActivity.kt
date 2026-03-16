package com.example.astronomy

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.astronomy.data.PlanetsData

class PlanetInfoActivity : Activity() {

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
        val imageView = findViewById<ImageView>(R.id.planet_image)
        val backButton = findViewById<Button>(R.id.btn_back)

        // Заполняем данные
        if (planetInfo != null) {
            nameView.text = planetInfo.name
            descriptionView.text = planetInfo.description
            imageView.setImageResource(planetInfo.imageResId)

            // Можно установить цвет акцента
            window.decorView.setBackgroundColor(planetInfo.color)
        } else {
            nameView.text = planetName
            descriptionView.text = "Информация о планете временно отсутствует."
        }

        // Кнопка назад
        backButton.setOnClickListener {
            finish()
        }
    }
}
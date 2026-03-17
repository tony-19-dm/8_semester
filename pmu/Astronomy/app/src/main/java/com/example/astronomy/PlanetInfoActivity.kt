package com.example.astronomy

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.astronomy.data.PlanetsData
import com.example.astronomy.utils.WaterTextureGenerator

class PlanetInfoActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planet_info)

        val planetName = intent.getStringExtra("planet_name") ?: "Земля"
        val planetInfo = PlanetsData.getInfo(planetName)

        val nameView = findViewById<TextView>(R.id.planet_name)
        val descriptionView = findViewById<TextView>(R.id.planet_description)
        val imageView = findViewById<ImageView>(R.id.planet_image)
        val backButton = findViewById<Button>(R.id.btn_back)
        val view3DButton = findViewById<Button>(R.id.btn_view_3d)

        if (planetInfo != null) {
            nameView.text = planetInfo.name
            descriptionView.text = planetInfo.description

            // Для Нептуна показываем кнопку 3D-просмотра
            if (planetName == "Нептун") {
                view3DButton.visibility = android.view.View.VISIBLE
                view3DButton.setOnClickListener {
                    val intent = Intent(this, NeptuneDetailActivity::class.java)
                    startActivity(intent)
                }

                // Показываем статичную превьюшку
                val waterTexture = WaterTextureGenerator.generateWaterTexture(256, 256, 0f)
                imageView.setImageBitmap(waterTexture)
            } else {
                view3DButton.visibility = android.view.View.GONE
                imageView.setImageResource(planetInfo.imageResId)
            }

            window.decorView.setBackgroundColor(planetInfo.color)
        }

        backButton.setOnClickListener {
            finish()
        }
    }
}
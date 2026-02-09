package com.example.astronomy

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.astronomy.opengl.MyGLSurfaceView

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenGLScreen() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("3D Сцена: Квадрат и Куб") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Описание
            Text(
                text = "Квадрат с текстурой галактики (фон) и вращающийся куб",
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp)
            )

            // OpenGL View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            ) {
                AndroidView(
                    factory = { ctx ->
                        MyGLSurfaceView(ctx).apply {
                            // Настройки OpenGL
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Легенда
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Описание сцены:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Квадрат отодвинут назад и увеличен как фон")
                    Text("• На квадрат наложена текстура галактики")
                    Text("• Куб расположен по центру экрана")
                    Text("• Куб вращается вокруг своей оси")
                }
            }
        }
    }
}
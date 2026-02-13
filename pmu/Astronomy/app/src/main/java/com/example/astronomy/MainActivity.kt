package com.example.astronomy

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // ✅ Передаём контекст явно
                NewsScreen(context = this)
            }
        }
    }
}

@Composable
fun NewsScreen(
    context: Context, // 👈 явный параметр
    newsViewModel: NewsViewModel = viewModel()
) {
    val currentNews by newsViewModel.newsList.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Новостной справочник",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.weight(1f)) {
                    if (currentNews.isNotEmpty()) {
                        NewsItemCard(
                            newsItem = currentNews[0],
                            onLikeClick = { newsViewModel.likeNews(currentNews[0].id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (currentNews.size >= 2) {
                        NewsItemCard(
                            newsItem = currentNews[1],
                            onLikeClick = { newsViewModel.likeNews(currentNews[1].id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(modifier = Modifier.weight(1f)) {
                    if (currentNews.size >= 3) {
                        NewsItemCard(
                            newsItem = currentNews[2],
                            onLikeClick = { newsViewModel.likeNews(currentNews[2].id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (currentNews.size >= 4) {
                        NewsItemCard(
                            newsItem = currentNews[3],
                            onLikeClick = { newsViewModel.likeNews(currentNews[3].id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ✅ Используем переданный context, без LocalContext.current
            Button(
                onClick = {
                    context.startActivity(Intent(context, GalaxyActivity::class.java))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("🌌 Перейти в 3D: Галактика и Куб")
            }
        }
    }
}

@Composable
fun NewsItemCard(
    newsItem: NewsItem,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedLikes by animateIntAsState(
        targetValue = newsItem.totalLikes,
        label = "likesAnimation"
    )

    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxSize(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .padding(16.dp)
            ) {
                Text(
                    text = newsItem.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = newsItem.content,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
                Text(
                    text = "ID: ${newsItem.id}",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.1f),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onLikeClick,
                    enabled = newsItem.canLike,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (newsItem.canLike) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "❤️", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$animatedLikes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (!newsItem.canLike) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "✓",
                                fontSize = 16.sp,
                                color = Color.Green,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
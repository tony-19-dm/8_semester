package com.example.astronomy

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
                NewsScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(newsViewModel: NewsViewModel = viewModel()) {
    // Подписываемся на изменения
    val currentNews by newsViewModel.newsList.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Заголовок
            Text(
                text = "Новостной справочник",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary
            )

            // Сетка новостей
            Column(modifier = Modifier.weight(1f)) {
                // Первая строка
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

                // Вторая строка
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
        }
    }
}

@Composable
fun NewsItemCard(
    newsItem: NewsItem,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Анимация для счетчика лайков
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
            // Верхняя часть (90%) - новость
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .padding(16.dp)
            ) {
                // Заголовок
                Text(
                    text = newsItem.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Содержание
                Text(
                    text = newsItem.content,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )

                // ID новости для отладки
                Text(
                    text = "ID: ${newsItem.id}",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Нижняя часть (10%) - лайки
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.1f),
                contentAlignment = Alignment.Center
            ) {
                // Кнопка лайка
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
                        Text(
                            text = "❤️",
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Анимированный счетчик
                        Text(
                            text = "$animatedLikes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        // Статус лайка
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
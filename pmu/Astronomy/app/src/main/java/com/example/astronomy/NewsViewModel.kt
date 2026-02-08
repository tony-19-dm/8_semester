package com.example.astronomy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class NewsViewModel : ViewModel() {

    // Хранилище всех новостей с их состоянием
    private val _allNewsStorage = mutableListOf<NewsItem>()

    // Текущие 4 новости на экране (только ID)
    private val _currentNewsIds = MutableStateFlow<List<Int>>(emptyList())

    // Поток для обновления UI
    private val _newsList = MutableStateFlow<List<NewsItem>>(emptyList())
    val newsList: StateFlow<List<NewsItem>> = _newsList.asStateFlow()

    init {
        initializeNews()
        startNewsRotation()
    }

    private fun initializeNews() {
        // Создаем 10 новостей
        _allNewsStorage.clear()
        for (i in 1..10) {
            _allNewsStorage.add(
                NewsItem(
                    id = i,
                    title = "Новость $i",
                    content = "Содержание новости $i. Это очень интересная новость, которую стоит прочитать полностью."
                )
            )
        }

        // Выбираем первые 4 новости
        val initialIds = _allNewsStorage.take(4).map { it.id }
        _currentNewsIds.value = initialIds
        updateNewsList()
    }

    // Обновляем список новостей для UI
    private fun updateNewsList() {
        val currentIds = _currentNewsIds.value
        val currentNews = currentIds.mapNotNull { id ->
            _allNewsStorage.find { it.id == id }
        }
        _newsList.value = currentNews
    }

    // Лайк новости
    fun likeNews(id: Int) {
        val newsIndex = _allNewsStorage.indexOfFirst { it.id == id }
        if (newsIndex != -1) {
            val newsItem = _allNewsStorage[newsIndex]

            // Проверяем, можно ли лайкнуть в текущем показе
            if (newsItem.canLike) {
                // Создаем копию с измененными полями
                val updatedItem = newsItem.copy(
                    totalLikes = newsItem.totalLikes + 1,
                    canLike = false
                )

                // Заменяем в хранилище
                _allNewsStorage[newsIndex] = updatedItem

                // Обновляем UI
                updateNewsList()
            }
        }
    }

    // Запуск таймера для замены новостей
    private fun startNewsRotation() {
        viewModelScope.launch {
            while (true) {
                delay(5000) // 5 секунд
                replaceRandomNews()
            }
        }
    }

    // Замена случайной новости
    private fun replaceRandomNews() {
        val currentIds = _currentNewsIds.value.toMutableList()

        // 1. Выбираем случайный индекс для замены (0-3)
        val indexToReplace = Random.nextInt(4)
        val newsIdToReplace = currentIds[indexToReplace]

        // 2. Сбрасываем возможность лайка для уходящей новости
        val newsIndexToReplace = _allNewsStorage.indexOfFirst { it.id == newsIdToReplace }
        if (newsIndexToReplace != -1) {
            val oldNews = _allNewsStorage[newsIndexToReplace]
            _allNewsStorage[newsIndexToReplace] = oldNews.copy(canLike = true)
        }

        // 3. Выбираем новую случайную новость из тех, которых нет на экране
        val availableNews = _allNewsStorage.filter {
            it.id !in currentIds
        }

        if (availableNews.isNotEmpty()) {
            val newNews = availableNews.random()

            // 4. Сбрасываем возможность лайка для новой новости
            val newNewsIndex = _allNewsStorage.indexOfFirst { it.id == newNews.id }
            if (newNewsIndex != -1) {
                _allNewsStorage[newNewsIndex] = newNews.copy(canLike = true)
            }

            // 5. Обновляем текущие ID
            currentIds[indexToReplace] = newNews.id
            _currentNewsIds.value = currentIds

            // 6. Обновляем UI
            updateNewsList()
        }
    }

    // Получить новость по ID (для отладки)
    fun getNewsById(id: Int): NewsItem? {
        return _allNewsStorage.find { it.id == id }
    }
}
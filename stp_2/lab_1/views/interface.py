from PyQt5.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QLabel, QLineEdit, QPushButton, QTableWidget, QTableWidgetItem,
    QMessageBox, QHeaderView, QMenuBar, QMenu, QAction, QStatusBar
)
from PyQt5.QtCore import Qt, pyqtSignal
from PyQt5.QtGui import QFont
from models.subscriber_list import SubscriberList



class PhoneBookWindow(QMainWindow):
    """Основное окно приложения"""
    def __init__(self):
        super().__init__()
        self.subscriber_list = SubscriberList()
        self.current_search = None
        self.init_ui()
        self.load_data()
    
    def init_ui(self):
        self.setWindowTitle('Телефонная книга')
        self.setGeometry(100, 100, 900, 700)
        
        # Центральный виджет
        central_widget = QWidget()
        self.setCentralWidget(central_widget)
        layout = QVBoxLayout(central_widget)
        
        # Заголовок
        title = QLabel('📖 Телефонная книга')
        title_font = QFont('Helvetica', 24, QFont.Bold)
        title.setFont(title_font)
        title.setAlignment(Qt.AlignCenter)
        layout.addWidget(title)
        
        # Поля ввода
        input_layout = QHBoxLayout()
        
        name_layout = QVBoxLayout()
        name_label = QLabel('Имя:')
        self.name_input = QLineEdit()
        name_layout.addWidget(name_label)
        name_layout.addWidget(self.name_input)
        
        phone_layout = QVBoxLayout()
        phone_label = QLabel('Телефон:')
        self.phone_input = QLineEdit()
        phone_layout.addWidget(phone_label)
        phone_layout.addWidget(self.phone_input)
        
        input_layout.addLayout(name_layout)
        input_layout.addLayout(phone_layout)
        layout.addLayout(input_layout)
        
        # Кнопки управления
        button_layout = QHBoxLayout()
        
        self.add_button = QPushButton('➕ Добавить')
        self.add_button.clicked.connect(self.add_subscriber)
        self.add_button.setStyleSheet('padding: 8px; font-weight: bold;')
        
        self.edit_button = QPushButton('✏️ Редактировать')
        self.edit_button.clicked.connect(self.edit_subscriber)
        self.edit_button.setEnabled(False)
        self.edit_button.setStyleSheet('padding: 8px;')
        
        self.delete_button = QPushButton('❌ Удалить')
        self.delete_button.clicked.connect(self.delete_subscriber)
        self.delete_button.setEnabled(False)
        self.delete_button.setStyleSheet('padding: 8px;')
        
        self.clear_button = QPushButton('🧹 Очистить книгу')
        self.clear_button.clicked.connect(self.clear_book)
        self.clear_button.setStyleSheet('padding: 8px;')
        
        button_layout.addWidget(self.add_button)
        button_layout.addWidget(self.edit_button)
        button_layout.addWidget(self.delete_button)
        button_layout.addWidget(self.clear_button)
        layout.addLayout(button_layout)
        
        # Поиск
        search_layout = QHBoxLayout()
        search_label = QLabel('🔍 Поиск:')
        self.search_input = QLineEdit()
        self.search_input.setPlaceholderText('Введите имя для поиска...')
        self.search_input.returnPressed.connect(self.search_subscribers)
        search_button = QPushButton('Найти')
        search_button.clicked.connect(self.search_subscribers)
        clear_search_button = QPushButton('Очистить поиск')
        clear_search_button.clicked.connect(self.clear_search)
        
        search_layout.addWidget(search_label)
        search_layout.addWidget(self.search_input)
        search_layout.addWidget(search_button)
        search_layout.addWidget(clear_search_button)
        layout.addLayout(search_layout)
        
        # Таблица абонентов
        self.table = QTableWidget()
        self.table.setColumnCount(2)
        self.table.setHorizontalHeaderLabels(['Имя', 'Телефон'])
        self.table.horizontalHeader().setSectionResizeMode(0, QHeaderView.Stretch)
        self.table.horizontalHeader().setSectionResizeMode(1, QHeaderView.ResizeToContents)
        self.table.setSelectionBehavior(QTableWidget.SelectRows)
        self.table.setEditTriggers(QTableWidget.NoEditTriggers)
        self.table.cellClicked.connect(self.on_row_selected)
        
        layout.addWidget(self.table)
        
        # Статус бар
        self.status_bar = QStatusBar()
        self.setStatusBar(self.status_bar)
        self.status_bar.showMessage('Готово')
        
        # Создаем меню
        self.create_menu()
    
    def create_menu(self):
        menubar = self.menuBar()
        
        # Меню Файл
        file_menu = menubar.addMenu('Файл')
        
        save_action = QAction('Сохранить', self)
        save_action.triggered.connect(self.save_data)
        file_menu.addAction(save_action)
        
        load_action = QAction('Загрузить', self)
        load_action.triggered.connect(self.load_data)
        file_menu.addAction(load_action)
        
        file_menu.addSeparator()
        
        exit_action = QAction('Выход', self)
        exit_action.triggered.connect(self.close)
        file_menu.addAction(exit_action)
        
        # Меню Справка
        help_menu = menubar.addMenu('Справка')
        
        about_action = QAction('О программе', self)
        about_action.triggered.connect(self.show_about)
        help_menu.addAction(about_action)
    
    def load_data(self):
        self.subscriber_list.load()
        self.update_table()
        self.status_bar.showMessage(f'Загружено записей: {len(self.subscriber_list.subscribers)}', 3000)
    
    def save_data(self):
        if self.subscriber_list.save():
            self.status_bar.showMessage('Данные сохранены', 3000)
        else:
            QMessageBox.warning(self, 'Ошибка', 'Не удалось сохранить данные')
    
    def update_table(self, subscribers=None):
        if subscribers is None:
            subscribers = self.subscriber_list.get_all()
        
        self.table.setRowCount(len(subscribers))
        for i, sub in enumerate(subscribers):
            self.table.setItem(i, 0, QTableWidgetItem(sub.name))
            self.table.setItem(i, 1, QTableWidgetItem(sub.phone))
    
    def on_row_selected(self, row, column):
        self.edit_button.setEnabled(True)
        self.delete_button.setEnabled(True)
        
        name_item = self.table.item(row, 0)
        phone_item = self.table.item(row, 1)
        
        if name_item and phone_item:
            self.name_input.setText(name_item.text())
            self.phone_input.setText(phone_item.text())
    
    def add_subscriber(self):
        name = self.name_input.text().strip()
        phone = self.phone_input.text().strip()
        
        if not name or not phone:
            QMessageBox.warning(self, 'Ошибка', 'Заполните все поля!')
            return
        
        if self.subscriber_list.add(name, phone):
            self.update_table()
            self.clear_inputs()
            self.status_bar.showMessage(f'Добавлен: {name}', 3000)
        else:
            QMessageBox.warning(self, 'Ошибка', 'Абонент уже существует!')
    
    def edit_subscriber(self):
        selected = self.table.selectedItems()
        if not selected:
            QMessageBox.warning(self, 'Ошибка', 'Выберите абонента для редактирования!')
            return
        
        row = selected[0].row()
        old_name = self.table.item(row, 0).text()
        
        name = self.name_input.text().strip()
        phone = self.phone_input.text().strip()
        
        if not name or not phone:
            QMessageBox.warning(self, 'Ошибка', 'Заполните все поля!')
            return
        
        if self.subscriber_list.edit(row, name, phone):
            self.update_table()
            self.clear_inputs()
            self.edit_button.setEnabled(False)
            self.delete_button.setEnabled(False)
            self.status_bar.showMessage(f'Обновлен: {old_name} → {name}', 3000)
        else:
            QMessageBox.warning(self, 'Ошибка', 'Не удалось обновить абонента!')
    
    def delete_subscriber(self):
        selected = self.table.selectedItems()
        if not selected:
            QMessageBox.warning(self, 'Ошибка', 'Выберите абонента для удаления!')
            return
        
        row = selected[0].row()
        name = self.table.item(row, 0).text()
        
        reply = QMessageBox.question(self, 'Подтверждение', 
                                   f'Удалить абонента "{name}"?',
                                   QMessageBox.Yes | QMessageBox.No)
        
        if reply == QMessageBox.Yes:
            if self.subscriber_list.delete(row):
                self.update_table()
                self.clear_inputs()
                self.edit_button.setEnabled(False)
                self.delete_button.setEnabled(False)
                self.status_bar.showMessage(f'Удален: {name}', 3000)
            else:
                QMessageBox.warning(self, 'Ошибка', 'Не удалось удалить абонента!')
    
    def search_subscribers(self):
        search_text = self.search_input.text().strip()
        if not search_text:
            self.update_table()
            return
        
        results = self.subscriber_list.search(search_text)
        self.current_search = results
        self.update_table(results)
        self.status_bar.showMessage(f'Найдено записей: {len(results)}', 3000)
    
    def clear_search(self):
        self.search_input.clear()
        self.current_search = None
        self.update_table()
        self.status_bar.showMessage('Поиск очищен', 3000)
    
    def clear_book(self):
        reply = QMessageBox.question(self, 'Подтверждение',
                                   'Очистить всю телефонную книгу?',
                                   QMessageBox.Yes | QMessageBox.No)
        
        if reply == QMessageBox.Yes:
            self.subscriber_list.clear()
            self.update_table()
            self.clear_inputs()
            self.status_bar.showMessage('Телефонная книга очищена', 3000)
    
    def clear_inputs(self):
        self.name_input.clear()
        self.phone_input.clear()
    
    def show_about(self):
        about_text = """Телефонная книга
        

Функции:
• Добавление/редактирование/удаление
• Поиск по имени
• Сохранение в файл
• Сортировка по имени"""
        
        QMessageBox.about(self, 'О программе', about_text)
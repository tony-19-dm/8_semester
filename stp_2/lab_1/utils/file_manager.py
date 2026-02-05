import pickle
import json
import os
from typing import List, Dict, Any

class FileManager:
    """Класс для работы с файлами (аналог 'Файл' из диаграммы)"""
    
    def __init__(self, filename: str = "data/phonebook.dat"):
        self.filename = filename
        os.makedirs(os.path.dirname(filename), exist_ok=True)
    
    def save(self, data: List[Dict[str, str]]) -> bool:
        """Сохранение данных в файл"""
        try:
            with open(self.filename, 'wb') as file:
                pickle.dump(data, file)
            return True
        except Exception as e:
            print(f"Ошибка сохранения: {e}")
            return False
    
    def load(self) -> List[Dict[str, str]]:
        """Загрузка данных из файла"""
        if not os.path.exists(self.filename):
            return []
        
        try:
            with open(self.filename, 'rb') as file:
                data = pickle.load(file)
                return data if isinstance(data, list) else []
        except (EOFError, pickle.UnpicklingError, FileNotFoundError) as e:
            print(f"Ошибка загрузки: {e}")
            return []
    
    def export_to_json(self, data: List[Dict[str, str]], 
                      filename: str = "data/phonebook_export.json") -> bool:
        """Экспорт данных в JSON"""
        try:
            os.makedirs(os.path.dirname(filename), exist_ok=True)
            with open(filename, 'w', encoding='utf-8') as file:
                json.dump(data, file, ensure_ascii=False, indent=2)
            return True
        except Exception as e:
            print(f"Ошибка экспорта: {e}")
            return False
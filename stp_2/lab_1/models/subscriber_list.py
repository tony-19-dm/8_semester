import os
import pickle
from .subscriber import Subscriber
class SubscriberList:
    """Класс списка абонентов"""
    def __init__(self):
        self.subscribers = []
        self.filename = "data/phonebook.dat"
        os.makedirs(os.path.dirname(self.filename), exist_ok=True)
    
    def add(self, name, phone):
        if not name or not phone:
            return False
        
        new_sub = Subscriber(name, phone)
        if new_sub in self.subscribers:
            return False
        
        self.subscribers.append(new_sub)
        self.subscribers.sort()
        self.save()
        return True
    
    def edit(self, index, name, phone):
        if 0 <= index < len(self.subscribers):
            self.subscribers[index] = Subscriber(name, phone)
            self.subscribers.sort()
            self.save()
            return True
        return False
    
    def delete(self, index):
        if 0 <= index < len(self.subscribers):
            del self.subscribers[index]
            self.save()
            return True
        return False
    
    def search(self, name):
        name_lower = name.lower()
        return [s for s in self.subscribers if name_lower in s.name.lower()]
    
    def clear(self):
        self.subscribers.clear()
        self.save()
    
    def load(self):
        if os.path.exists(self.filename):
            try:
                with open(self.filename, 'rb') as f:
                    data = pickle.load(f)
                    self.subscribers = [Subscriber.from_dict(d) for d in data]
                    self.subscribers.sort()
            except:
                self.subscribers = []
    
    def save(self):
        try:
            data = [s.to_dict() for s in self.subscribers]
            with open(self.filename, 'wb') as f:
                pickle.dump(data, f)
            return True
        except:
            return False
    
    def get_all(self):
        return self.subscribers.copy()
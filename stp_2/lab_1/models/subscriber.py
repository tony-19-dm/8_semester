class Subscriber:
    """Класс абонента"""
    def __init__(self, name, phone):
        self.name = name.strip()
        self.phone = phone.strip()
    
    def __eq__(self, other):
        if isinstance(other, Subscriber):
            return self.name == other.name and self.phone == other.phone
        return False
    
    def __lt__(self, other):
        return self.name.lower() < other.name.lower()
    
    def to_dict(self):
        return {"name": self.name, "phone": self.phone}
    
    @classmethod
    def from_dict(cls, data):
        return cls(data["name"], data["phone"])
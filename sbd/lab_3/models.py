from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

db = SQLAlchemy()

class Car(db.Model):
    __tablename__ = 'cars'
    
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(20), nullable=False)
    brand = db.Column(db.String(20), nullable=False)
    description = db.Column(db.Text)
    price = db.Column(db.Numeric(10, 2), nullable=False)
    stock = db.Column(db.Integer, nullable=False, default=0)
    
    def __repr__(self):
        return f'<Car {self.brand} {self.name}>'
    
    @property
    def price_formatted(self):
        return f"{self.price:,.2f} ₽"

# MongoDB модели будут обрабатываться напрямую через pymongo
class Comment:
    collection_name = 'comments'
    
    @staticmethod
    def get_collection(mongo_db):
        return mongo_db[Comment.collection_name]
    
    @staticmethod
    def create_document(product_id, author, text, rating):
        return {
            'product_id': product_id,
            'author': author if author else "Аноним",
            'text': text,
            'rating': rating,
            'created_at': datetime.utcnow()
        }
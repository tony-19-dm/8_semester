from flask import Flask, render_template, redirect, url_for, flash, request
from pymongo import MongoClient
from datetime import datetime
from config import Config
from models import db, Car, Comment
from forms import CarForm, CommentForm, SearchForm

app = Flask(__name__)
app.config.from_object(Config)

# Инициализация PostgreSQL
db.init_app(app)

# Инициализация MongoDB
mongo_client = MongoClient(app.config['MONGO_URI'])
mongo_db = mongo_client[app.config['MONGO_DB']]
comments_collection = Comment.get_collection(mongo_db)

# Создание индекса для MongoDB
comments_collection.create_index('product_id')

@app.route('/')
def index():
    cars = Car.query.all()
    return render_template('index.html', cars=cars)

@app.route('/cars/new', methods=['GET', 'POST'])
def add_car():
    form = CarForm()
    
    if form.validate_on_submit():
        car = Car(
            name=form.name.data,
            brand=form.brand.data,
            description=form.description.data,
            price=form.price.data,
            stock=form.stock.data
        )
        
        db.session.add(car)
        db.session.commit()
        
        flash('Автомобиль успешно добавлен!', 'success')
        return redirect(url_for('index'))
    
    return render_template('add_car.html', form=form)

@app.route('/cars/<int:product_id>', methods=['GET', 'POST'])
def car_detail(product_id):
    car = Car.query.get_or_404(product_id)
    form = CommentForm()
    
    # Получение отзывов из MongoDB
    comments = list(comments_collection.find({'product_id': product_id}).sort('created_at', -1))
    
    if form.validate_on_submit():
        # Создание нового отзыва в MongoDB
        comment = Comment.create_document(
            product_id=product_id,
            author=form.author.data,
            text=form.text.data,
            rating=form.rating.data
        )
        
        comments_collection.insert_one(comment)
        flash('Отзыв успешно добавлен!', 'success')
        return redirect(url_for('car_detail', product_id=product_id))
    
    return render_template('car_detail.html', car=car, comments=comments, form=form)

@app.route('/search', methods=['GET', 'POST'])
def search():
    form = SearchForm()
    results = []
    search_performed = False
    
    if form.validate_on_submit():
        search_performed = True
        query = form.query.data
        min_rating = form.min_rating.data
        
        # Базовый запрос к PostgreSQL
        car_query = Car.query
        
        if query:
            car_query = car_query.filter(Car.name.ilike(f'%{query}%'))
        
        cars = car_query.all()
        
        # Получение среднего рейтинга из MongoDB для каждого автомобиля
        for car in cars:
            # Агрегация для вычисления среднего рейтинга
            pipeline = [
                {'$match': {'product_id': car.id}},
                {'$group': {
                    '_id': '$product_id',
                    'avg_rating': {'$avg': '$rating'},
                    'count': {'$sum': 1}
                }}
            ]
            
            result = list(comments_collection.aggregate(pipeline))
            
            if result and result[0]['count'] > 0:
                avg_rating = round(result[0]['avg_rating'], 1)
                rating_count = result[0]['count']
            else:
                avg_rating = None
                rating_count = 0
            
            # Фильтрация по минимальному рейтингу
            if min_rating > 0:
                if avg_rating is not None and avg_rating >= min_rating:
                    results.append({
                        'car': car,
                        'avg_rating': avg_rating,
                        'rating_count': rating_count
                    })
            else:
                results.append({
                    'car': car,
                    'avg_rating': avg_rating,
                    'rating_count': rating_count
                })
    
    return render_template('search.html', form=form, results=results, search_performed=search_performed)

@app.cli.command("init-db")
def init_db():
    """Инициализация базы данных"""
    db.create_all()
    print("База данных PostgreSQL инициализирована")

if __name__ == '__main__':
    app.run(debug=True)
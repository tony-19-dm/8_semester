from flask_wtf import FlaskForm
from wtforms import StringField, TextAreaField, IntegerField, SelectField, DecimalField
from wtforms.validators import DataRequired, Length, NumberRange, Optional

class CarForm(FlaskForm):
    name = StringField('Название модели', validators=[
        DataRequired(), 
        Length(max=20, message='Название должно быть не длиннее 20 символов')
    ])
    brand = StringField('Марка', validators=[
        DataRequired(), 
        Length(max=20, message='Марка должна быть не длиннее 20 символов')
    ])
    description = TextAreaField('Описание', validators=[Optional()])
    price = DecimalField('Цена', validators=[
        DataRequired(), 
        NumberRange(min=0.01, message='Цена должна быть положительной')
    ])
    stock = IntegerField('Количество на складе', validators=[
        DataRequired(), 
        NumberRange(min=0, message='Количество не может быть отрицательным')
    ])

class CommentForm(FlaskForm):
    author = StringField('Ваше имя', validators=[Optional()])
    text = TextAreaField('Отзыв', validators=[
        DataRequired(message='Текст отзыва не может быть пустым')
    ])
    rating = SelectField('Оценка', choices=[
        (5, '5 - Отлично'),
        (4, '4 - Хорошо'),
        (3, '3 - Средне'),
        (2, '2 - Плохо'),
        (1, '1 - Ужасно')
    ], coerce=int, validators=[DataRequired()])

class SearchForm(FlaskForm):
    query = StringField('Поиск по названию', validators=[Optional()])
    min_rating = SelectField('Минимальный рейтинг', choices=[
        (0, 'Любой'),
        (1, '1+'),
        (2, '2+'),
        (3, '3+'),
        (4, '4+'),
        (5, '5')
    ], coerce=int, validators=[Optional()])
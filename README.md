# java-filmorate
Template repository for Filmorate project.

# Diagram 

![database architecture](images/diagram.jpg)


## Примеры запросов для основных операций

### Получение всех фильмов
`SELECT *
FROM Film`

Получение всех пользователей
SELECT *
FROM User

Топ N наиболее популярных фильмов
SELECT Film.name
FROM Film 
LEFT JOIN Rating ON Film.ratingID = Rating.ratingID
ORDER BY Rating.ratingID
LIMIT N

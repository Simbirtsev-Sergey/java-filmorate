MERGE INTO Applications(application_id, name) KEY (application_id)
VALUES (1, 'CONFIRMED'),
       (2, 'UNCONFIRMED');

MERGE INTO Genres(genre_id, name) KEY (genre_id)
VALUES (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Мультфильм'),
       (4, 'Триллер'),
       (5, 'Документальный'),
       (6, 'Боевик');

MERGE INTO Ratings(rating_id, name) KEY (rating_id)
VALUES (1, 'G'),
       (2, 'PG'),
       (3, 'PG-13'),
       (4, 'R'),
       (5, 'NC-17');

MERGE INTO Users(user_id, email, login, name, birthday) KEY (user_id)
VALUES (1, 'cerga@mail.ru', 'cerega', 'Сергей', '2005-11-05'),
       (2, 'ivan@mail.ru', 'ivan', 'Иван', '2006-04-12'),
       (3, 'edick@mail.ru', 'edick', 'Эдуард', '2016-01-11'),
       (4, 'misha@mail.ru', 'misha', 'Михаил', '2006-04-05'),
       (5, 'point@mail.ru', 'point', 'Пётр', '2008-12-31'),
       (6, 'zxc@mail.ru', 'zxc', 'Николай', '1998-05-01');

MERGE INTO Films(film_id, name, description, release_date, duration, rating_id) KEY (film_id)
VALUES (1, 'Колобок', 'Замес года', '2024-03-15', 130, 3),
       (2, 'Движение вверх', 'Роковые 3 секунды', '2017-12-14', 150, 2),
       (3, 'Матрица', 'Единички нолики', '1999-03-31', 160, 2),
       (4, 'Последний богатырь', 'Посланник тьмы', '2021-12-23', 134, 5),
       (5, 'Человек паук', 'Возвращение домой', '2017-07-06', 140, 4);

MERGE INTO Film_genres(film_id, genre_id) KEY (film_id, genre_id)
VALUES (1, 1),
       (1, 4),
       (1, 5),
       (2, 2),
       (3, 3),
       (3, 4),
       (3, 5),
       (4, 4),
       (4, 5),
       (5, 1);

MERGE INTO Film_likes(user_id, film_id) KEY (user_id, film_id)
VALUES (1, 1),
       (1, 3),
       (2, 3),
       (2, 5),
       (3, 4),
       (5, 5);

MERGE INTO Friendships(sender_id, recipient_id, application_id) KEY (sender_id, recipient_id)
VALUES (1, 2, 1),
       (2, 1, 1),
       (2, 3, 2),
       (1, 4, 1),
       (4, 1, 1),
       (4, 5, 2),
       (3, 4, 2),
       (5, 1, 2);

ALTER TABLE Users ALTER COLUMN user_id RESTART WITH (SELECT COALESCE(MAX(user_id), 0) + 1 FROM Users);
ALTER TABLE Films ALTER COLUMN film_id RESTART WITH (SELECT COALESCE(MAX(film_id), 0) + 1 FROM Films);
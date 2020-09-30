SELECT title, rating
FROM movies
JOIN ratings
on id = movie_id
where year = 2010
ORDER BY rating DESC, title;
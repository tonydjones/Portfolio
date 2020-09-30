SELECT DISTINCT name
FROM people
JOIN stars
ON people.id = stars.person_id
JOIN movies
ON stars.movie_id = movies.id
WHERE people.id IN (
    SELECT person_id
    FROM stars
    JOIN people
    ON stars.person_id = people.id
    JOIN movies
    ON stars.movie_id = movies.id
    WHERE stars.movie_id IN (
        SELECT movies.id
        FROM movies
        JOIN stars
        ON movies.id = stars.movie_id
        JOIN people
        ON stars.person_id = people.id
        WHERE name = "Kevin Bacon" AND birth = 1958)
    EXCEPT
    SELECT people.id
    FROM people
    WHERE name = "Kevin Bacon" AND birth = 1958);
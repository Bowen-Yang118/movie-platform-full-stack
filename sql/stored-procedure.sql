DELIMITER $$

CREATE PROCEDURE add_movie(
	IN p_title VARCHAR(100), 
    IN p_year INT, 
    IN p_director VARCHAR(100), 
    IN p_star_name VARCHAR(100), 
    IN p_genre_name VARCHAR(32),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;
    -- Check if movie already exists
    SELECT id INTO movie_id FROM movies
    WHERE title = p_title AND year = p_year AND director = p_director;
    
    IF movie_id IS NOT NULL THEN
        SET p_message = 'common.parser.Movie already exists.';
    ELSE
		-- SELECT CONCAT('tt', LPAD(
-- 			IFNULL( (SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) FROM movies) + 1, 1), 
-- 			7, '0'
-- 		)) INTO movie_id;
		SELECT CONCAT('tt', LPAD(
            IFNULL(
                (SELECT MAX(CAST(SUBSTRING(id, 3, 7) AS UNSIGNED)) 
                 FROM movies 
                 WHERE id REGEXP '^tt[0-9]{7}'
                ) + 1, 1
            ), 
            7, '0'
        )) INTO movie_id;
        
        -- Insert new movie
        INSERT INTO movies (id, title, year, director) VALUES (movie_id, p_title, p_year, p_director);
		
        -- Check if star exists
        SELECT id INTO star_id FROM stars WHERE name = p_star_name LIMIT 1;
		IF star_id IS NULL THEN
            -- Generate new star ID
			-- SELECT CONCAT( 'nm', LPAD(
-- 				IFNULL( (SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) FROM stars) + 1, 1), 
-- 				7, '0'
-- 			)) INTO star_id;
			SELECT CONCAT('nm', LPAD(
            IFNULL(
                (SELECT MAX(CAST(SUBSTRING(id, 3, 7) AS UNSIGNED)) 
                 FROM stars 
                 WHERE id REGEXP '^nm[0-9]{7}'
                ) + 1, 1
            ), 
            7, '0'
			)) INTO star_id;
			-- Insert new star
				INSERT INTO stars (id, name) VALUES (star_id, p_star_name);
		END IF;
        
        -- Check if genre exists
        SELECT id INTO genre_id FROM genres WHERE name = p_genre_name LIMIT 1;
        IF genre_id IS NULL THEN
			INSERT INTO genres (name) VALUES (p_genre_name);
			SET genre_id = LAST_INSERT_ID();
        END IF;
        
        -- Link movie to star
        -- Link movie to star only if the relationship does not exist
		IF NOT EXISTS (SELECT 1 FROM stars_in_movies WHERE starId = star_id AND movieId = movie_id) THEN
			INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);
		END IF;
        -- INSERT INTO stars_in_movies (starId, movieId) VALUES (star_id, movie_id);
        -- Link movie to genre
        -- Link movie to genre only if the relationship does not exist
		IF NOT EXISTS (SELECT 1 FROM genres_in_movies WHERE genreId = genre_id AND movieId = movie_id) THEN
			INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);
		END IF;
        -- INSERT INTO genres_in_movies (genreId, movieId) VALUES (genre_id, movie_id);
        SET p_message = 'common.parser.Movie added successfully.';
	END IF;
END$$

DELIMITER ;



DELIMITER $$

CREATE PROCEDURE add_star(
    IN p_name VARCHAR(100), 
    IN p_birth_year INT, 
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE star_id VARCHAR(10);
    -- Check if the star already exists (by name and birth year)
    SELECT id INTO star_id FROM stars WHERE name = p_name AND (birthYear = p_birth_year) LIMIT 1;
    IF star_id IS NOT NULL THEN
        SET p_message = 'Star already exists.';
	ELSE
        -- Generate a new star ID (non-sequential handling)
        -- SELECT CONCAT('nm', LPAD(
--             IFNULL((SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) FROM stars) + 1, 1), 
--             7, '0'
--         )) INTO star_id;
		SELECT CONCAT('nm', LPAD(
            IFNULL(
                (SELECT MAX(CAST(SUBSTRING(id, 3, 7) AS UNSIGNED)) 
                 FROM stars 
                 WHERE id REGEXP '^nm[0-9]{7}'
                ) + 1, 1
            ), 
            7, '0'
        )) INTO star_id;
        -- Insert new star into database
        INSERT INTO stars (id, name, birthYear) VALUES (star_id, p_name, p_birth_year);
        SET p_message = 'Star added successfully.';
	END IF;
END $$

DELIMITER ;

package fr.charlesmassuard.mymovies_api.repository;

import fr.charlesmassuard.mymovies_api.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Integer> {
}

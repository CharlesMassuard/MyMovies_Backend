package fr.charlesmassuard.mymovies_api.repository;

import fr.charlesmassuard.mymovies_api.model.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Integer> {
}

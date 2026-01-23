package fr.charlesmassuard.mymovies_api.repository;

import fr.charlesmassuard.mymovies_api.model.UserMovie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface UserMovieRepository extends JpaRepository<UserMovie, Integer> {
    java.util.Optional<UserMovie> findByUserIdAndMovieId(Integer userId, int movieId);
    java.util.List<UserMovie> findAllByUserId(Integer userId);

    @Modifying
    @Transactional
    void deleteAllByUserId(Integer userId);
}

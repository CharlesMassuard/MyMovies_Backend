package fr.charlesmassuard.mymovies_api.repository;

import fr.charlesmassuard.mymovies_api.model.UserSerie;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import jakarta.transaction.Transactional;

@Repository
public interface UserSerieRepository extends JpaRepository<UserSerie, Integer> {
    java.util.Optional<UserSerie> findByUserIdAndSerieId(Integer userId, int serieId);
    java.util.List<UserSerie> findAllByUserId(Integer userId);

    @Modifying
    @Transactional
    void deleteAllByUserId(Integer userId);
}

package fr.charlesmassuard.mymovies_api.repository;

import fr.charlesmassuard.mymovies_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByPseudo(String pseudo);
    boolean existsByMail(String mail);
    Optional<User> findByMail(String mail);
    Optional<User> findByResetToken(String resetToken);
}

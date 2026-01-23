package fr.charlesmassuard.mymovies_api.service;

import fr.charlesmassuard.mymovies_api.model.User;
import fr.charlesmassuard.mymovies_api.dto.UserDTO;
import fr.charlesmassuard.mymovies_api.exceptions.UserException;
import fr.charlesmassuard.mymovies_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import fr.charlesmassuard.mymovies_api.config.JwtUtils;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import fr.charlesmassuard.mymovies_api.repository.UserMovieRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMovieRepository userMovieRepository;

    private static final String USER_NOT_FOUND = "User not found";

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, UserMovieRepository userMovieRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userMovieRepository = userMovieRepository;
    }

    public UserDTO createUser(String pseudo, String mail, String password) throws UserException {
        if (userRepository.existsByPseudo(pseudo)) {
            throw new UserException("Pseudo already in use");
        }
        if (userRepository.existsByMail(mail)) {
            throw new UserException("Email already in use");
        }
        User user = User.builder()
                .pseudo(pseudo)
                .mail(mail)
                .password(passwordEncoder.encode(password))
                .registrationDate(LocalDateTime.now())
                .lastLoginDate(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return UserDTO.builder()
                .pseudo(user.getPseudo())
                .mail(user.getMail())
                .password(user.getPassword())
                .registrationDate(user.getRegistrationDate())
                .lastLoginDate(user.getLastLoginDate())
                .build();
    }

    public UserDTO authenticateUser(String mail, String password) throws UserException {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserException("Invalid password");
        }

        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);

        return UserDTO.builder()
                .pseudo(user.getPseudo())
                .mail(user.getMail())
                .password(user.getPassword())
                .registrationDate(user.getRegistrationDate())
                .lastLoginDate(user.getLastLoginDate())
                .build();
    }

    public void updateUserPseudo(String mail, String newPseudo) throws UserException {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        if (userRepository.existsByPseudo(newPseudo)) {
            throw new UserException("Pseudo already in use");
        }

        user.setPseudo(newPseudo);
        userRepository.save(user);
    }

    public String updateUserMail(String currentMail, String newMail) throws UserException {
        User user = userRepository.findByMail(currentMail)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        if (userRepository.existsByMail(newMail)) {
            throw new UserException("Email already in use");
        }

        user.setMail(newMail);
        userRepository.save(user);
        return jwtUtils.generateToken(newMail);
    }

    public void updateUserPassword(String mail, String currentPassword, String newPassword) throws UserException {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UserException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String mail) throws UserException {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new UserException(USER_NOT_FOUND));

        userMovieRepository.deleteAllByUserId(user.getId());

        userRepository.delete(user);
    }
}


package fr.charlesmassuard.mymovies_api.service;

import fr.charlesmassuard.mymovies_api.model.User;
import fr.charlesmassuard.mymovies_api.dto.UserDTO;
import fr.charlesmassuard.mymovies_api.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO createUser(String pseudo, String mail, String password) {
        if (userRepository.existsByPseudo(pseudo)) {
            throw new RuntimeException("Pseudo already in use");
        }
        if (userRepository.existsByMail(mail)) {
            throw new RuntimeException("Email already in use");
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

    public UserDTO authenticateUser(String mail, String password) {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
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

    public void updateUserPseudo(String mail, String newPseudo) {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userRepository.existsByPseudo(newPseudo)) {
            throw new RuntimeException("Pseudo already in use");
        }

        user.setPseudo(newPseudo);
        userRepository.save(user);
    }

    public void updateUserMail(String currentMail, String newMail) {
        User user = userRepository.findByMail(currentMail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userRepository.existsByMail(newMail)) {
            throw new RuntimeException("Email already in use");
        }

        user.setMail(newMail);
        userRepository.save(user);
    }

    public void updateUserPassword(String mail, String currentPassword, String newPassword) {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void deleteUser(String mail) {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
    }
}


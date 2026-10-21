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

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;

@Service
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserMovieRepository userMovieRepository;
    private final I18nService i18n;

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, UserMovieRepository userMovieRepository, I18nService i18n, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.userMovieRepository = userMovieRepository;
        this.i18n = i18n;
        this.mailSender = mailSender;
    }

    public UserDTO createUser(String pseudo, String mail, String password) throws UserException {
        if (pseudo == null || pseudo.isBlank()) {
            throw new UserException(i18n.get("error.pseudoRequired"));
        }
        if (mail == null || mail.isBlank()) {
            throw new UserException(i18n.get("error.emailRequired"));
        }
        if (password == null || password.isBlank()) {
            throw new UserException(i18n.get("error.passwordRequired"));
        }
        pseudo = pseudo.trim();
        mail = mail.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(mail).matches()) {
            throw new UserException(i18n.get("error.invalidEmail"));
        }
        if (password.length() < 6) {
            throw new UserException(i18n.get("error.passwordTooShort"));
        }
        if (userRepository.existsByMail(mail)) {
            throw new UserException(i18n.get("error.emailTaken"));
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
        if (mail == null || mail.isBlank()) {
            throw new UserException(i18n.get("error.emailRequired"));
        }
        if (password == null || password.isBlank()) {
            throw new UserException(i18n.get("error.passwordRequired"));
        }
        mail = mail.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(mail).matches()) {
            throw new UserException(i18n.get("error.invalidEmail"));
        }
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserException(i18n.get("error.invalidPassword"));
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
        if (newPseudo == null || newPseudo.isBlank()) {
            throw new UserException(i18n.get("error.pseudoRequired"));
        }
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));

        newPseudo = newPseudo.trim();

        user.setPseudo(newPseudo);
        userRepository.save(user);
    }

    public String updateUserMail(String currentMail, String newMail, String currentPassword) throws UserException {
        User user = userRepository.findByMail(currentMail)
                .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
                
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UserException(i18n.get("error.currentPassword"));
        }

        if (newMail == null || newMail.isBlank() || !EMAIL_PATTERN.matcher(newMail.trim()).matches()) {
            throw new UserException(i18n.get("error.invalidEmail"));
        }
        newMail = newMail.trim().toLowerCase();
        if (userRepository.existsByMail(newMail)) {
            throw new UserException(i18n.get("error.emailTaken"));
        }

        user.setMail(newMail);
        userRepository.save(user);
        return jwtUtils.generateToken(newMail);
    }

    public void updateUserPassword(String mail, String currentPassword, String newPassword) throws UserException {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));

        if (newPassword == null || newPassword.isBlank()) {
            throw new UserException(i18n.get("error.passwordRequired"));
        }
        if (newPassword.length() < 6) {
            throw new UserException(i18n.get("error.passwordTooShort"));
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new UserException(i18n.get("error.currentPassword"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String mail) throws UserException {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));

        userMovieRepository.deleteAllByUserId(user.getId());

        userRepository.delete(user);
    }

    public void processForgotPassword(String mail) throws UserException {
        User user = userRepository.findByMail(mail)
                .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));

        //generationdutokenunique
        String token = UUID.randomUUID().toString();
        
        //expirationdans1heure
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        //constructiondelurldynamique
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            
            helper.setFrom("charles@charlesmassuard.com", "MyMovies");
            helper.setTo(user.getMail());
            helper.setSubject(i18n.get("auth.resetEmailSubject"));
            
            String mailContent = i18n.get("auth.resetEmailBody") + "\n" 
                            + resetUrl + "\n\n" 
                            + i18n.get("auth.resetEmailExpiry");
            
            helper.setText(mailContent);
            mailSender.send(message);
            
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new UserException("Erreur lors de l'envoi de l'email");
        }
    }

    public void resetPassword(String token, String newPassword) throws UserException {
        if (newPassword == null || newPassword.isBlank()) {
            throw new UserException(i18n.get("error.passwordRequired"));
        }
        if (newPassword.length() < 6) {
            throw new UserException(i18n.get("error.passwordTooShort"));
        }
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new UserException(i18n.get("error.invalidToken")));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new UserException(i18n.get("error.expiredToken"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}
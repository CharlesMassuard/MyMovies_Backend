package fr.charlesmassuard.mymovies_api.controller;

import fr.charlesmassuard.mymovies_api.config.JwtUtils;
import fr.charlesmassuard.mymovies_api.dto.UserDTO;
import fr.charlesmassuard.mymovies_api.exceptions.UserException;
import fr.charlesmassuard.mymovies_api.service.UserService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private static final String MESSAGE = "message";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String TOKEN = "token";
    private static final String REFRESH_TOKEN = "refreshToken";
    private static final String MESSAGE_ERROR = "An unexpected error occurred";

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken != null && jwtUtils.validateToken(refreshToken)) {
            String email = jwtUtils.getMailFromToken(refreshToken);
            
            String newAccessToken = jwtUtils.generateToken(email);
            String newRefreshToken = jwtUtils.generateRefreshToken(email);
            
            return ResponseEntity.ok(Map.of(
                "token", newAccessToken,
                "refreshToken", newRefreshToken
            ));
        }
        
        System.out.println("Refus du refreshToken !");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> credentials) {
        try {
            String mail = credentials.get("mail");
            String password = credentials.get("password");
            UserDTO user = userService.authenticateUser(mail, password);
            String token = jwtUtils.generateToken(mail);
            String refreshToken = jwtUtils.generateRefreshToken(mail);
            return ResponseEntity.ok(Map.of(
                    STATUS, SUCCESS,
                    MESSAGE, "User authenticated successfully",
                    TOKEN, token,
                    REFRESH_TOKEN, refreshToken,
                    "user", user
            ));
        } catch (UserException e) { // <-- Remplacement de RuntimeException par UserException
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                STATUS, ERROR,
                MESSAGE, e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    STATUS, ERROR,
                    MESSAGE, MESSAGE_ERROR
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody UserDTO user) {
        try {
            UserDTO createdUser = userService.createUser(user.getPseudo(), user.getMail(), user.getPassword());
            String token = jwtUtils.generateToken(user.getMail());
            String refreshToken = jwtUtils.generateRefreshToken(user.getMail());
            return ResponseEntity.ok(Map.of(
                    STATUS, SUCCESS,
                    MESSAGE, "User created successfully",
                    TOKEN, token,
                    REFRESH_TOKEN, refreshToken,
                    "user", createdUser
            ));
        } catch (UserException e) { // <-- Remplacement ici aussi
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                STATUS, ERROR,
                MESSAGE, e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    STATUS, ERROR,
                    MESSAGE, MESSAGE_ERROR
            ));
        }
    }

    @PutMapping("/update/pseudo")
    public ResponseEntity<Map<String, Object>> updateUserPseudo(Principal principal, @RequestBody Map<String, String> request) {
        try {
            String currentMail = principal.getName();
            String newPseudo = request.get("newPseudo");
            userService.updateUserPseudo(currentMail, newPseudo);
            return ResponseEntity.ok(Map.of(
                    STATUS, SUCCESS,
                    MESSAGE, "User pseudo updated successfully"
            ));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                STATUS, ERROR,
                MESSAGE, e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    STATUS, ERROR,
                    MESSAGE, MESSAGE_ERROR
            ));
        }
    }

    @PutMapping("/update/mail")
    public ResponseEntity<Map<String, Object>> updateUserMail(Principal principal, @RequestBody Map<String, String> request) {
        try {
            String currentMail = principal.getName();
            String newMail = request.get("newMail");
            String currentPassword = request.get("currentPassword");
            String newToken = userService.updateUserMail(currentMail, newMail, currentPassword);
            return ResponseEntity.ok(Map.of(
                    STATUS, SUCCESS,
                    MESSAGE, "User mail updated successfully",
                    TOKEN, newToken
            ));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                STATUS, ERROR,
                MESSAGE, e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    STATUS, ERROR,
                    MESSAGE, MESSAGE_ERROR
            ));
        }
    }

    @PutMapping("/update/password")
    public ResponseEntity<Map<String, Object>> updateUserPassword(Principal principal, @RequestBody Map<String, String> request) {
        try {
            String currentMail = principal.getName();
            String newPassword = request.get("newPassword");
            String currentPassword = request.get("oldPassword");
            userService.updateUserPassword(currentMail, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of(
                    STATUS, SUCCESS,
                    MESSAGE, "User password updated successfully"
            ));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                STATUS, ERROR,
                MESSAGE, e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    STATUS, ERROR,
                    MESSAGE, MESSAGE_ERROR
            ));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(Principal principal) {
        try {
            String mail = principal.getName();
            userService.deleteUser(mail);
            return ResponseEntity.ok(Map.of(
                    STATUS, SUCCESS,
                    MESSAGE, "User deleted successfully"
            ));
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                STATUS, ERROR,
                MESSAGE, e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    STATUS, ERROR,
                    MESSAGE, MESSAGE_ERROR
            ));
        }
    }
}
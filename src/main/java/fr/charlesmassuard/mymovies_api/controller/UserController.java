package fr.charlesmassuard.mymovies_api.controller;

import fr.charlesmassuard.mymovies_api.config.JwtUtils;
import fr.charlesmassuard.mymovies_api.dto.UserDTO;
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
    private static final String MESSAGE_ERROR = "An unexpected error occurred";

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody Map<String, String> credentials) {
        try {
            String mail = credentials.get("mail");
            String password = credentials.get("password");
            UserDTO user = userService.authenticateUser(mail, password);
            String token = jwtUtils.generateToken(mail);
            return ResponseEntity.ok(Map.of(
                    STATUS, SUCCESS,
                    MESSAGE, "User authenticated successfully",
                    TOKEN, token,
                    "user", user
            ));
        } catch (RuntimeException e) {
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
            return ResponseEntity.ok(Map.of(
                    STATUS, SUCCESS,
                    MESSAGE, "User created successfully",
                    TOKEN, token,
                    "user", createdUser
            ));
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
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
            String newToken = userService.updateUserMail(currentMail, newMail);
            return ResponseEntity.ok(Map.of(
                    STATUS, SUCCESS,
                    MESSAGE, "User mail updated successfully",
                    TOKEN, newToken
            ));
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
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
        } catch (RuntimeException e) {
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

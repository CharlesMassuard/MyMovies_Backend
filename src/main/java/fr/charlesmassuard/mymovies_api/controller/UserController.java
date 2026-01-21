package fr.charlesmassuard.mymovies_api.controller;

import fr.charlesmassuard.mymovies_api.config.JwtUtils;
import fr.charlesmassuard.mymovies_api.dto.UserDTO;
import fr.charlesmassuard.mymovies_api.service.UserService;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> credentials) {
        try {
            String mail = credentials.get("mail");
            String password = credentials.get("password");
            UserDTO user = userService.authenticateUser(mail, password);
            String token = jwtUtils.generateToken(mail);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "User authenticated successfully",
                    "token", token,
                    "user", user
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "An unexpected error occurred"
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> addUser(@RequestBody UserDTO user) {
        try {
            UserDTO createdUser = userService.createUser(user.getPseudo(), user.getMail(), user.getPassword());
            String token = jwtUtils.generateToken(user.getMail());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "User created successfully",
                    "token", token,
                    "user", createdUser
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "An unexpected error occurred"
            ));
        }
    }
}

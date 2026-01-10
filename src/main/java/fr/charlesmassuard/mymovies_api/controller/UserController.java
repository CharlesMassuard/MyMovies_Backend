package fr.charlesmassuard.mymovies_api.controller;

import fr.charlesmassuard.mymovies_api.dto.UserDTO;
import fr.charlesmassuard.mymovies_api.service.UserService;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody UserDTO user) {
        try {
            UserDTO createdUser = userService.createUser(user.getPseudo(), user.getMail(), user.getPassword());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "User created successfully",
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

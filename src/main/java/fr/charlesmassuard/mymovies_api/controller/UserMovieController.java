package fr.charlesmassuard.mymovies_api.controller;

import fr.charlesmassuard.mymovies_api.dto.UserMovieDTO;
import fr.charlesmassuard.mymovies_api.service.UserMovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.security.Principal;

@RestController
@RequestMapping("/api/user/movies")
@RequiredArgsConstructor
public class UserMovieController {


    public record StatusRequest(String status) {}
    private final UserMovieService userMovieService;
    
    @PostMapping("/to-watch/{movieId}")
    public ResponseEntity<String> addUserFilm(@PathVariable int movieId, Principal principal) {
        String userEmail = principal.getName();
        userMovieService.addToWatchlist(userEmail, movieId);
        return ResponseEntity.ok("Film ajouté à la liste");
    }

    @GetMapping("/status/{movieId}")
    public ResponseEntity<String> getUserMovieStatus(@PathVariable int movieId, Principal principal) {
        String userEmail = principal.getName();
        String status = userMovieService.getUserMovieStatus(userEmail, movieId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/watched-date/{movieId}")
    public ResponseEntity<String> getUserMovieWatchedDate(@PathVariable int movieId, Principal principal) {
        String userEmail = principal.getName();
        String watchedDate = userMovieService.getUserMovieWatchedDate(userEmail, movieId);
        return ResponseEntity.ok(watchedDate);
    }

    @PutMapping("/status/{movieId}")
    public ResponseEntity<String> updateUserMovieStatus(
        @PathVariable int movieId, 
        @RequestBody StatusRequest request, 
        Principal principal
    ) {
        String userEmail = principal.getName();
        userMovieService.updateUserMovieStatus(userEmail, movieId, request.status());
        return ResponseEntity.ok("Statut du film mis à jour");
    }

    @DeleteMapping("/status/{movieId}")
    public ResponseEntity<String> deleteUserMovieStatus(@PathVariable int movieId, Principal principal) {
        String userEmail = principal.getName();
        userMovieService.deleteUserMovieStatus(userEmail, movieId);
        return ResponseEntity.ok("Film supprimé de la liste");
    }
}

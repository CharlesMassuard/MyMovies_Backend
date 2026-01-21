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
}

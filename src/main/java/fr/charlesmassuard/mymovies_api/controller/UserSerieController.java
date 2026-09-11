package fr.charlesmassuard.mymovies_api.controller;

import fr.charlesmassuard.mymovies_api.service.UserSerieService;
import fr.charlesmassuard.mymovies_api.dto.UserSerieResponseDTO;
import fr.charlesmassuard.mymovies_api.exceptions.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user/series")
@RequiredArgsConstructor
public class UserSerieController {

    public record StatusRequest(String status, String watchedAt) {}
    public record RateRequest(int rating, String comment) {}
    
    private final UserSerieService userSerieService;
    
    @PostMapping("/to-watch/{serieId}")
    public ResponseEntity<String> addUserSerie(@PathVariable int serieId, Principal principal) throws UserException {
        String userEmail = principal.getName();
        userSerieService.addToWatchlist(userEmail, serieId);
        return ResponseEntity.ok("Série ajoutée à la liste");
    }

    @GetMapping
    public ResponseEntity<List<UserSerieResponseDTO>> getUserSeries(Principal principal) throws UserException {
        String userEmail = principal.getName();
        List<UserSerieResponseDTO> userSeries = userSerieService.getUserSeries(userEmail);
        return ResponseEntity.ok(userSeries);
    }

    @GetMapping("/status/{serieId}")
    public ResponseEntity<String> getUserSerieStatus(@PathVariable int serieId, Principal principal) throws UserException {
        String userEmail = principal.getName();
        String status = userSerieService.getUserSerieStatus(userEmail, serieId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/watched-date/{serieId}")
    public ResponseEntity<String> getUserSerieWatchedDate(@PathVariable int serieId, Principal principal) throws UserException {
        String userEmail = principal.getName();
        String watchedDate = userSerieService.getUserSerieWatchedDate(userEmail, serieId);
        return ResponseEntity.ok(watchedDate);
    }

    @GetMapping("/rating/{serieId}")
    public ResponseEntity<Integer> getUserSerieRating(@PathVariable int serieId, Principal principal) throws UserException {
        String userEmail = principal.getName();
        Integer rating = userSerieService.getUserSerieRating(userEmail, serieId);
        return ResponseEntity.ok(rating);
    }

    @GetMapping("/comment/{serieId}")
    public ResponseEntity<String> getUserSerieComment(@PathVariable int serieId, Principal principal) throws UserException {
        String userEmail = principal.getName();
        String comment = userSerieService.getUserSerieComment(userEmail, serieId);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/status/{serieId}")
    public ResponseEntity<String> updateUserSerieStatus(
        @PathVariable int serieId, 
        @RequestBody StatusRequest request, 
        Principal principal
    ) throws UserException {
        String userEmail = principal.getName();
        userSerieService.updateUserSerieStatus(userEmail, serieId, request.status(), request.watchedAt());
        return ResponseEntity.ok("Statut de la série mis à jour");
    }

    @PutMapping("/rate/{serieId}")
    public ResponseEntity<String> rateUserSerie(
        @PathVariable int serieId, 
        @RequestBody RateRequest request,
        Principal principal
    )throws UserException  {
        String userEmail = principal.getName();
        userSerieService.rateUserSerie(userEmail, serieId, request.rating(), request.comment());
        return ResponseEntity.ok("Note de la série mise à jour");
    }

    @DeleteMapping("/status/{serieId}")
    public ResponseEntity<String> deleteUserSerieStatus(@PathVariable int serieId, Principal principal) throws UserException {
        String userEmail = principal.getName();
        userSerieService.deleteUserSerieStatus(userEmail, serieId);
        return ResponseEntity.ok("Série supprimée de la liste");
    }
}
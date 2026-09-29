package fr.charlesmassuard.mymovies_api.controller;

import fr.charlesmassuard.mymovies_api.service.TmdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/person")
public class PersonController {

    private final TmdbService tmdbService;

    public PersonController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getPersonDetails(@PathVariable String id) {
        try {
            String response = tmdbService.getPersonDetails(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/credits")
    public ResponseEntity<String> getPersonCredits(@PathVariable String id) {
        try {
            String response = tmdbService.getPersonCredits(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/external_ids")
    public ResponseEntity<String> getPersonExternalIds(@PathVariable String id) {
        try {
            String response = tmdbService.getPersonExternalIds(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
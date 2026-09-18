package fr.charlesmassuard.mymovies_api.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.charlesmassuard.mymovies_api.dto.RecommendationItemDTO;
import fr.charlesmassuard.mymovies_api.exceptions.UserException;
import fr.charlesmassuard.mymovies_api.service.RecommendationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/general")
    public ResponseEntity<List<RecommendationItemDTO>> getGeneralRecommendations(
            Principal principal,
            @RequestParam(defaultValue = "false") boolean forceRefresh) throws UserException {
        
        if (principal == null) {
            return ResponseEntity.ok(List.of());
        }
        List<RecommendationItemDTO> recommendations = recommendationService.getGeneralRecommendations(principal.getName(), forceRefresh);
        return ResponseEntity.ok(recommendations);
    }
}
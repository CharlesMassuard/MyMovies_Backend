package fr.charlesmassuard.mymovies_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import fr.charlesmassuard.mymovies_api.service.TmdbService;

@RestController
@RequestMapping("/api/series")
@RequiredArgsConstructor
public class SerieController {

    private final TmdbService tmdb;

    @GetMapping("/trending")
    public String trendingSeries() {
        return tmdb.getTrendingSeries();
    }

    @GetMapping("/trending/day")
    public String trendingSeriesDay() {
        return tmdb.getTrendingSeriesDay();
    }

    @GetMapping("/search")
    public String searchSeries(@RequestParam String query) {
        return tmdb.searchSeries(query);
    }

    @GetMapping("/{id}")
    public String getSerieDetails(@PathVariable String id) {
        return tmdb.getSerieDetails(id);
    }

    @GetMapping("/{id}/credits")
    public String getSerieCredits(@PathVariable String id) {
        return tmdb.getSerieCredits(id);
    }

    @GetMapping("/{id}/season/{seasonNumber}")
    public String getSerieSeasonDetails(@PathVariable String id, @PathVariable String seasonNumber) {
        return tmdb.getSerieSeasonDetails(id, seasonNumber);
    }

    @GetMapping("/{id}/season/{seasonNumber}/episode/{episodeNumber}")
    public String getSerieEpisodeDetails(@PathVariable String id, @PathVariable String seasonNumber, @PathVariable String episodeNumber) {
        return tmdb.getSerieEpisodeDetails(id, seasonNumber, episodeNumber);
    }
}
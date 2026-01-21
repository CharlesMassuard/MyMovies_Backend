package fr.charlesmassuard.mymovies_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import fr.charlesmassuard.mymovies_api.service.TmdbService;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final TmdbService tmdb;

    @GetMapping("/trending")
    public String trendingMovies() {
        return tmdb.getTrendingMovies();
    }

    @GetMapping("/in-theater")
    public String moviesInTheater() {
        return tmdb.getMoviesInTheater();
    }

    @GetMapping("/trending/day")
    public String trendingMoviesDay() {
        return tmdb.getTrendingMoviesDay();
    }

    @GetMapping("/search")
    public String searchMovies(@RequestParam String query) {
        return tmdb.searchMovies(query);
    }

    @GetMapping("/{id}")
    public String getMovieDetails(@PathVariable String id) {
        return tmdb.getMovieDetails(id);
    }

    @GetMapping("/{id}/credits")
    public String getMovieCredits(@PathVariable String id) {
        return tmdb.getMovieCredits(id);
    }
}
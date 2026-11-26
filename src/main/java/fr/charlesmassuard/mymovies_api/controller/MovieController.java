package fr.charlesmassuard.mymovies_api.controller;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import fr.charlesmassuard.mymovies_api.service.TmdbService;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final TmdbService tmdb;

    @GetMapping("/spotlight")
    public List<Map<String, Object>> spotlight() {
        return tmdb.getSpotlight();
    }

    @GetMapping("/genres")
    public String movieGenres() {
        return tmdb.getMovieGenres();
    }

    @GetMapping("/discover")
    public String discoverMovies(
        @RequestParam(required = false) String genre,
        @RequestParam(required = false) Integer year,
        @RequestParam(required = false) Double minRating,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) String provider,
        @RequestParam(defaultValue = "1") int page
    ) {
        return tmdb.discoverMovies(genre, year, minRating, sortBy, provider, page);
    }

    @GetMapping("/trending")
    public String trendingMovies() {
        return tmdb.getTrendingMovies();
    }

    @GetMapping("/in-theater")
    public String moviesInTheater() {
        return tmdb.getMoviesInTheater();
    }

    @GetMapping("/upcoming")
    public String upcomingMovies() {
        return tmdb.getUpcomingMovies();
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

    //Nouvelles-routes-pour-le-front
    @GetMapping("/{id}/similar")
    public String getSimilarMovies(@PathVariable String id) {
        return tmdb.getMovieSimilar(id);
    }

    @GetMapping("/{id}/videos")
    public String getMovieVideos(@PathVariable String id) {
        return tmdb.getMovieVideos(id);
    }

    @GetMapping("/{id}/providers")
    public String getMovieProviders(@PathVariable String id) {
        return tmdb.getMovieProviders(id);
    }

    @GetMapping("/collection/{id}")
    public String getCollectionDetails(@PathVariable String id) {
        return tmdb.getCollectionDetails(id);
    }
}
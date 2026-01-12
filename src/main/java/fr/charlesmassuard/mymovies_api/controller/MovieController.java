package fr.charlesmassuard.mymovies_api.controller;

import org.springframework.web.bind.annotation.*;

import fr.charlesmassuard.mymovies_api.service.TmdbService;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    private final TmdbService tmdb;

    public MovieController(TmdbService tmdb) {
        this.tmdb = tmdb;
    }

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
}


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

    @GetMapping("/test")
    public String testTmdb() {
        return tmdb.getAuthStatus();
    }
}


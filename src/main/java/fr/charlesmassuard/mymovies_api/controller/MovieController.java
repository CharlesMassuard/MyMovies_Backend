package fr.charlesmassuard.mymovies_api.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/movies")
public class MovieController {

    @GetMapping
    public String getMovies() {
        return "Backend Spring Boot OK";
    }
}

package fr.charlesmassuard.mymovies_api.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@Service
public class TmdbService {

    private final RestClient client;

    public TmdbService(@Value("${tmdb.api.token}") String token) {
        this.client = RestClient.builder()
            .baseUrl("https://api.themoviedb.org/3")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    public String getTrendingMovies() {
        return client.get()
            .uri("/discover/movie?include_adult=false&language=fr-FR&page=1&sort_by=popularity.desc")
            .retrieve()
            .body(String.class);
    }

    public String getMoviesInTheater() {
        return client.get()
            .uri("/movie/now_playing?language=fr-FR&region=FR&include_adult=false&page=1&sort_by=popularity.desc")
            .retrieve()
            .body(String.class);
    }

    public String getTrendingMoviesDay() {
        return client.get()
            .uri("/trending/movie/day?language=fr-FR&include_adult=false&page=1&sort_by=popularity.desc&region=FR")
            .retrieve()
            .body(String.class);
    }

    public String searchMovies(String query) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search/movie")
                .queryParam("query", query)
                .queryParam("language", "fr-FR")
                .queryParam("include_adult", "false")
                .queryParam("page", "1")
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getMovieDetails(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/movie/" + id)
                .queryParam("language", "fr-FR")
                .build())
            .retrieve()
            .body(String.class);
    }

    public Map<String, Object> getMovieDetailsMap(int id) {
        return client.get()
            .uri("/movie/" + id + "?language=fr-FR")
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public String getMovieCredits(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/movie/" + id + "/credits")
                .queryParam("language", "fr-FR")
                .build())
            .retrieve()
            .body(String.class);
    }
}

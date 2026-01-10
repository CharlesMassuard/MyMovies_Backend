package fr.charlesmassuard.mymovies_api.tmdb;

import org.springframework.beans.factory.annotation.Value;
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

    public String getAuthStatus() {
        return client.get()
            .uri("/discover/movie?include_adult=false&include_video=false&language=en-US&page=1&sort_by=popularity.desc")
            .retrieve()
            .body(String.class);
    }
}

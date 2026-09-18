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

    private static final String MOVIE_URL = "/movie/";
    private static final String TV_URL = "/tv/";
    private static final String LANGUAGE = "language";
    private static final String LANGUAGE_FR = "fr-FR";

    public TmdbService(@Value("${tmdb.api.token}") String token) {
        this.client = RestClient.builder()
            .baseUrl("https://api.themoviedb.org/3")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    public String getTrendingMovies() {
        return client.get()
            .uri("/discover/movie?include_adult=false&language=" + LANGUAGE_FR + "&page=1&sort_by=popularity.desc")
            .retrieve()
            .body(String.class);
    }

    public String getTrendingSeries() {
        return client.get()
            .uri("/discover/tv?include_adult=false&language=" + LANGUAGE_FR + "&page=1&sort_by=popularity.desc")
            .retrieve()
            .body(String.class);
    }

    public String getMoviesInTheater() {
        return client.get()
            .uri("/movie/now_playing?language=" + LANGUAGE_FR + "&region=FR&include_adult=false&page=1&sort_by=popularity.desc")
            .retrieve()
            .body(String.class);
    }

    public String getTrendingMoviesDay() {
        return client.get()
            .uri("/trending/movie/day?language=" + LANGUAGE_FR + "&include_adult=false&page=1&sort_by=popularity.desc&region=FR")
            .retrieve()
            .body(String.class);
    }

    public String getTrendingSeriesDay() {
        return client.get()
            .uri("/trending/tv/day?language=" + LANGUAGE_FR + "&include_adult=false&page=1&sort_by=popularity.desc&region=FR")
            .retrieve()
            .body(String.class);
    }

    public String searchMovies(String query) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search/movie")
                .queryParam("query", query)
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .queryParam("include_adult", "false")
                .queryParam("page", "1")
                .build())
            .retrieve()
            .body(String.class);
    }

    public String searchSeries(String query) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search/tv")
                .queryParam("query", query)
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .queryParam("include_adult", "false")
                .queryParam("page", "1")
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getMovieDetails(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id)
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getSerieDetails(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id)
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .build())
            .retrieve()
            .body(String.class);
    }

    public Map<String, Object> getMovieDetailsMap(int id) {
        return client.get()
            .uri(MOVIE_URL  + id + "?language=" + LANGUAGE_FR)
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> getSerieDetailsMap(int id) {
        return client.get()
            .uri(TV_URL  + id + "?language=" + LANGUAGE_FR)
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public String getSerieSeasonDetails(String id, String seasonNumber) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/season/" + seasonNumber)
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getSerieEpisodeDetails(String id, String seasonNumber, String episodeNumber) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/season/" + seasonNumber + "/episode/" + episodeNumber)
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getMovieCredits(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id + "/credits")
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getSerieCredits(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/credits")
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .build())
            .retrieve()
            .body(String.class);
    }

    //Méthodes-pour-récupérer-les-similaires,-les-vidéos-et-les-plateformes
    public String getMovieSimilar(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id + "/recommendations")
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getSerieSimilar(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/recommendations")
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getMovieVideos(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id + "/videos")
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getSerieVideos(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/videos")
                .queryParam(LANGUAGE, LANGUAGE_FR)
                .build())
            .retrieve()
            .body(String.class);
    }

    //Note-les-providers-n'ont-pas-toujours-besoin-du-paramètre-langue-mais-c'est-mieux-pour-TMDB
    public String getMovieProviders(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id + "/watch/providers")
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getSerieProviders(String id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/watch/providers")
                .build())
            .retrieve()
            .body(String.class);
    }
}
package fr.charlesmassuard.mymovies_api.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TmdbService {

    private final RestClient client;

    private static final String MOVIE_URL = "/movie/";
    private static final String TV_URL = "/tv/";
    private static final String PERSON_URL = "/person/";
    private static final String LANGUAGE = "language";
    private final I18nService i18n;

    private static final Duration LIST_TTL = Duration.ofMinutes(15);
    private static final Duration DETAIL_TTL = Duration.ofMinutes(30);
    private static final int MAX_CACHE_ENTRIES = 500;

    private record CacheEntry<T>(T value, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, CacheEntry<Object>> cache = new ConcurrentHashMap<>();

    public TmdbService(@Value("${tmdb.api.token}") String token, I18nService i18n) {
        this.i18n = i18n;
        this.client = RestClient.builder()
            .baseUrl("https://api.themoviedb.org/3")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    @SuppressWarnings("unchecked")
    private <T> T cached(String key, Duration ttl, Supplier<T> loader) {
        CacheEntry<Object> existing = cache.get(key);
        if (existing != null && !existing.isExpired()) {
            return (T) existing.value();
        }
        if (cache.size() >= MAX_CACHE_ENTRIES) {
            cache.entrySet().removeIf(e -> e.getValue().isExpired());
            if (cache.size() >= MAX_CACHE_ENTRIES) {
                cache.clear();
            }
        }
        T fresh = loader.get();
        if (fresh != null) {
            cache.put(key, new CacheEntry<>(fresh, Instant.now().plus(ttl)));
        }
        return fresh;
    }

    public String getTrendingMovies() {
        String lang = i18n.tmdbLanguage();
        return cached("trending_movies:" + lang, LIST_TTL, () -> client.get()
            .uri("/discover/movie?include_adult=false&language=" + lang + "&page=1&sort_by=popularity.desc")
            .retrieve()
            .body(String.class));
    }

    public String getTrendingSeries() {
        String lang = i18n.tmdbLanguage();
        return cached("trending_series:" + lang, LIST_TTL, () -> client.get()
            .uri("/discover/tv?include_adult=false&language=" + lang + "&page=1&sort_by=popularity.desc")
            .retrieve()
            .body(String.class));
    }

    public String getMoviesInTheater() {
        String lang = i18n.tmdbLanguage();
        return cached("in_theater:" + lang, LIST_TTL, () -> client.get()
            .uri("/movie/now_playing?language=" + lang + "&region=FR&include_adult=false&page=1&sort_by=popularity.desc")
            .retrieve()
            .body(String.class));
    }

    public String getUpcomingMovies() {
        String lang = i18n.tmdbLanguage();
        return cached("upcoming_movies:" + lang, LIST_TTL, () -> client.get()
            .uri("/movie/upcoming?language=" + lang + "&region=FR&include_adult=false&page=1")
            .retrieve()
            .body(String.class));
    }

    public String getUpcomingSeries() {
        String lang = i18n.tmdbLanguage();
        String today = java.time.LocalDate.now().toString();
        return cached("upcoming_series:" + lang + ":" + today, LIST_TTL, () -> client.get()
            .uri("/discover/tv?include_adult=false&language=" + lang + "&page=1&sort_by=popularity.desc&first_air_date.gte=" + today)
            .retrieve()
            .body(String.class));
    }

    public String getTrendingMoviesDay() {
        String lang = i18n.tmdbLanguage();
        return cached("trending_movies_day:" + lang, LIST_TTL, () -> client.get()
            .uri("/trending/movie/day?language=" + lang + "&include_adult=false&page=1&sort_by=popularity.desc&region=FR")
            .retrieve()
            .body(String.class));
    }

    public String getTrendingSeriesDay() {
        String lang = i18n.tmdbLanguage();
        return cached("trending_series_day:" + lang, LIST_TTL, () -> client.get()
            .uri("/trending/tv/day?language=" + lang + "&include_adult=false&page=1&sort_by=popularity.desc&region=FR")
            .retrieve()
            .body(String.class));
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getSpotlight() {
        String lang = i18n.tmdbLanguage();
        String imgLangs = i18n.cacheSuffix() + ",en,null";
        return cached("spotlight:" + lang, LIST_TTL, () -> {
            Map<String, Object> moviesRes = client.get()
                .uri("/trending/movie/day?language=" + lang + "&include_adult=false&page=1&region=FR")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
            Map<String, Object> seriesRes = client.get()
                .uri("/trending/tv/day?language=" + lang + "&include_adult=false&page=1&region=FR")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            List<Map<String, Object>> combined = new ArrayList<>();
            if (moviesRes != null && moviesRes.get("results") instanceof List<?> mList) {
                for (Object o : mList) {
                    if (o instanceof Map<?, ?> m) {
                        Map<String, Object> copy = new HashMap<>((Map<String, Object>) m);
                        copy.put("media_type", "movie");
                        combined.add(copy);
                    }
                }
            }
            if (seriesRes != null && seriesRes.get("results") instanceof List<?> sList) {
                for (Object o : sList) {
                    if (o instanceof Map<?, ?> s) {
                        Map<String, Object> copy = new HashMap<>((Map<String, Object>) s);
                        copy.put("media_type", "serie");
                        combined.add(copy);
                    }
                }
            }

            combined.sort(Comparator.comparingDouble((Map<String, Object> m) -> {
                Object pop = m.get("popularity");
                return pop instanceof Number n ? n.doubleValue() : 0.0;
            }).reversed());

            List<Map<String, Object>> top = combined.stream().limit(8).toList();
            List<Map<String, Object>> enriched = new ArrayList<>();

            for (Map<String, Object> item : top) {
                try {
                    String type = String.valueOf(item.get("media_type"));
                    String id = String.valueOf(item.get("id"));
                    String path = "serie".equals(type) ? TV_URL + id : MOVIE_URL + id;
                    Map<String, Object> details = client.get()
                        .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam(LANGUAGE, lang)
                            .queryParam("append_to_response", "images")
                            .queryParam("include_image_language", imgLangs)
                            .build())
                        .retrieve()
                        .body(new ParameterizedTypeReference<Map<String, Object>>() {});

                    if (details != null) {
                        item.put("genres", details.get("genres"));
                        item.put("runtime", details.get("runtime"));
                        item.put("number_of_seasons", details.get("number_of_seasons"));
                        item.put("images", details.get("images"));
                    }
                } catch (Exception ignored) {
                    // Keep base item if enrichment fails
                }
                enriched.add(item);
            }
            return enriched;
        });
    }

    public String getMovieGenres() {
        String lang = i18n.tmdbLanguage();
        return cached("genres_movie:" + lang, DETAIL_TTL, () -> client.get()
            .uri("/genre/movie/list?language=" + lang)
            .retrieve()
            .body(String.class));
    }

    public String getSerieGenres() {
        String lang = i18n.tmdbLanguage();
        return cached("genres_tv:" + lang, DETAIL_TTL, () -> client.get()
            .uri("/genre/tv/list?language=" + lang)
            .retrieve()
            .body(String.class));
    }

    public String discoverMovies(String genre, Integer year, Double minRating, String sortBy, String provider, int page) {
        String lang = i18n.tmdbLanguage();
        String cacheKey = String.format("disc_m:%s:%s:%s:%s:%s:%s:%d", lang, genre, year, minRating, sortBy, provider, page);
        return cached(cacheKey, LIST_TTL, () -> client.get()
            .uri(uriBuilder -> {
                var b = uriBuilder.path("/discover/movie")
                    .queryParam(LANGUAGE, lang)
                    .queryParam("include_adult", "false")
                    .queryParam("page", page)
                    .queryParam("sort_by", (sortBy != null && !sortBy.isBlank()) ? sortBy : "popularity.desc");
                if (genre != null && !genre.isBlank()) b.queryParam("with_genres", genre);
                if (year != null) b.queryParam("primary_release_year", year);
                if (minRating != null && minRating > 0) {
                    b.queryParam("vote_average.gte", minRating);
                    b.queryParam("vote_count.gte", 50);
                }
                if (provider != null && !provider.isBlank()) {
                    b.queryParam("with_watch_providers", provider);
                    b.queryParam("watch_region", "FR");
                    b.queryParam("with_watch_monetization_types", "flatrate");
                }
                return b.build();
            })
            .retrieve()
            .body(String.class));
    }

    public String discoverSeries(String genre, Integer year, Double minRating, String sortBy, String provider, int page) {
        String lang = i18n.tmdbLanguage();
        String cacheKey = String.format("disc_s:%s:%s:%s:%s:%s:%s:%d", lang, genre, year, minRating, sortBy, provider, page);
        return cached(cacheKey, LIST_TTL, () -> client.get()
            .uri(uriBuilder -> {
                var b = uriBuilder.path("/discover/tv")
                    .queryParam(LANGUAGE, lang)
                    .queryParam("include_adult", "false")
                    .queryParam("page", page)
                    .queryParam("sort_by", (sortBy != null && !sortBy.isBlank()) ? sortBy : "popularity.desc");
                if (genre != null && !genre.isBlank()) b.queryParam("with_genres", genre);
                if (year != null) b.queryParam("first_air_date_year", year);
                if (minRating != null && minRating > 0) {
                    b.queryParam("vote_average.gte", minRating);
                    b.queryParam("vote_count.gte", 50);
                }
                if (provider != null && !provider.isBlank()) {
                    b.queryParam("with_watch_providers", provider);
                    b.queryParam("watch_region", "FR");
                    b.queryParam("with_watch_monetization_types", "flatrate");
                }
                return b.build();
            })
            .retrieve()
            .body(String.class));
    }

    public String searchMovies(String query) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search/movie")
                .queryParam("query", query)
                .queryParam(LANGUAGE, i18n.tmdbLanguage())
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
                .queryParam(LANGUAGE, i18n.tmdbLanguage())
                .queryParam("include_adult", "false")
                .queryParam("page", "1")
                .build())
            .retrieve()
            .body(String.class);
    }

    public String searchPeople(String query) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/search/person")
                .queryParam("query", query)
                .queryParam(LANGUAGE, i18n.tmdbLanguage())
                .queryParam("include_adult", "false")
                .queryParam("page", "1")
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getMovieDetails(String id) {
        String lang = i18n.tmdbLanguage();
        String imageLangs = i18n.cacheSuffix() + ",en,null";
        return cached("movie_details:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id)
                .queryParam(LANGUAGE, lang)
                .queryParam("append_to_response", "images,release_dates,credits,videos,recommendations,watch/providers")
                .queryParam("include_image_language", imageLangs)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getSerieDetails(String id) {
        String lang = i18n.tmdbLanguage();
        String imageLangs = i18n.cacheSuffix() + ",en,null";
        return cached("serie_details:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id)
                .queryParam(LANGUAGE, lang)
                .queryParam("append_to_response", "images,content_ratings,credits,videos,recommendations,watch/providers")
                .queryParam("include_image_language", imageLangs)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getCollectionDetails(String id) {
        String lang = i18n.tmdbLanguage();
        return cached("collection:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path("/collection/" + id)
                .queryParam(LANGUAGE, lang)
                .build())
            .retrieve()
            .body(String.class));
    }

    public Map<String, Object> getMovieDetailsMap(int id) {
        return client.get()
            .uri(MOVIE_URL + id + "?language=" + i18n.tmdbLanguage())
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> getSerieDetailsMap(int id) {
        String lang = i18n.tmdbLanguage();
        return cached("serie_map:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(TV_URL + id + "?language=" + lang)
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {}));
    }

    public String getSerieSeasonDetails(String id, String seasonNumber) {
        String lang = i18n.tmdbLanguage();
        return cached("season:" + id + ":" + seasonNumber + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/season/" + seasonNumber)
                .queryParam(LANGUAGE, lang)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getSerieEpisodeDetails(String id, String seasonNumber, String episodeNumber) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/season/" + seasonNumber + "/episode/" + episodeNumber)
                .queryParam(LANGUAGE, i18n.tmdbLanguage())
                .build())
            .retrieve()
            .body(String.class);
    }

    public String getMovieCredits(String id) {
        String lang = i18n.tmdbLanguage();
        return cached("movie_credits:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id + "/credits")
                .queryParam(LANGUAGE, lang)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getSerieCredits(String id) {
        String lang = i18n.tmdbLanguage();
        return cached("serie_credits:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/credits")
                .queryParam(LANGUAGE, lang)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getMovieSimilar(String id) {
        String lang = i18n.tmdbLanguage();
        return cached("movie_sim:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id + "/recommendations")
                .queryParam(LANGUAGE, lang)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getSerieSimilar(String id) {
        String lang = i18n.tmdbLanguage();
        return cached("serie_sim:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/recommendations")
                .queryParam(LANGUAGE, lang)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getMovieVideos(String id) {
        String lang = i18n.tmdbLanguage();
        return cached("movie_vid:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id + "/videos")
                .queryParam(LANGUAGE, lang)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getSerieVideos(String id) {
        String lang = i18n.tmdbLanguage();
        return cached("serie_vid:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/videos")
                .queryParam(LANGUAGE, lang)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getMovieProviders(String id) {
        return cached("movie_prov:" + id, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id + "/watch/providers")
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getSerieProviders(String id) {
        return cached("serie_prov:" + id, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/watch/providers")
                .build())
            .retrieve()
            .body(String.class));
    }

    public Map<String, Object> getMovieRecommendationsMap(int id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(MOVIE_URL + id + "/recommendations")
                .queryParam(LANGUAGE, i18n.tmdbLanguage())
                .build())
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public Map<String, Object> getSerieRecommendationsMap(int id) {
        return client.get()
            .uri(uriBuilder -> uriBuilder
                .path(TV_URL + id + "/recommendations")
                .queryParam(LANGUAGE, i18n.tmdbLanguage())
                .build())
            .retrieve()
            .body(new ParameterizedTypeReference<Map<String, Object>>() {});
    }

    public String getPersonDetails(String id) {
        String lang = i18n.tmdbLanguage();
        return cached("person_det:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(PERSON_URL + id)
                .queryParam(LANGUAGE, lang)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getPersonCredits(String id) {
        String lang = i18n.tmdbLanguage();
        return cached("person_cred:" + id + ":" + lang, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(PERSON_URL + id + "/combined_credits")
                .queryParam(LANGUAGE, lang)
                .build())
            .retrieve()
            .body(String.class));
    }

    public String getPersonExternalIds(String id) {
        return cached("person_ext:" + id, DETAIL_TTL, () -> client.get()
            .uri(uriBuilder -> uriBuilder
                .path(PERSON_URL + id + "/external_ids")
                .build())
            .retrieve()
            .body(String.class));
    }
}

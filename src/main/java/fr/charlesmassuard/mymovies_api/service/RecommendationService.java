package fr.charlesmassuard.mymovies_api.service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import fr.charlesmassuard.mymovies_api.dto.RecommendationItemDTO;
import fr.charlesmassuard.mymovies_api.exceptions.UserException;
import fr.charlesmassuard.mymovies_api.model.User;
import fr.charlesmassuard.mymovies_api.model.UserMovie;
import fr.charlesmassuard.mymovies_api.model.UserSerie;
import fr.charlesmassuard.mymovies_api.repository.UserMovieRepository;
import fr.charlesmassuard.mymovies_api.repository.UserRepository;
import fr.charlesmassuard.mymovies_api.repository.UserSerieRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final UserRepository userRepository;
    private final UserMovieRepository userMovieRepository;
    private final UserSerieRepository userSerieRepository;
    private final TmdbService tmdbService;

    // Système de cache en mémoire
    private static class CacheEntry {
        List<RecommendationItemDTO> recommendations;
        LocalDate generationDate;

        CacheEntry(List<RecommendationItemDTO> recommendations, LocalDate generationDate) {
            this.recommendations = recommendations;
            this.generationDate = generationDate;
        }
    }

    private final Map<String, CacheEntry> recommendationCache = new ConcurrentHashMap<>();

    public List<RecommendationItemDTO> getGeneralRecommendations(String userEmail, boolean forceRefresh) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException("Utilisateur non trouvé"));

        LocalDate today = LocalDate.now();

        // 1. Vérifier si on a un cache valide pour aujourd'hui
        if (!forceRefresh) {
            CacheEntry entry = recommendationCache.get(userEmail);
            if (entry != null && entry.generationDate.equals(today)) {
                return entry.recommendations; // On retourne le cache (la liste ne change pas)
            }
        }

        // 2. Si pas de cache, ou cache périmé, ou forceRefresh -> On recalcule
        List<UserMovie> allUserMovies = userMovieRepository.findAllByUserId(user.getId());
        List<UserSerie> allUserSeries = userSerieRepository.findAllByUserId(user.getId());

        Set<Integer> existingMovieIds = allUserMovies.stream()
            .map(um -> um.getMovie().getId())
            .collect(Collectors.toSet());

        Set<Integer> existingSerieIds = allUserSeries.stream()
            .map(us -> us.getSerie().getId())
            .collect(Collectors.toSet());

        List<UserMovie> seedMovies = allUserMovies.stream().filter(um -> um.getRating() >= 7).toList();
        if (seedMovies.isEmpty()) seedMovies = allUserMovies;

        List<UserSerie> seedSeries = allUserSeries.stream().filter(us -> us.getRating() >= 7).toList();
        if (seedSeries.isEmpty()) seedSeries = allUserSeries;

        List<UserMovie> shuffledMovies = new ArrayList<>(seedMovies);
        Collections.shuffle(shuffledMovies);

        List<UserSerie> shuffledSeries = new ArrayList<>(seedSeries);
        Collections.shuffle(shuffledSeries);

        Map<Integer, RecommendationItemDTO> movieRecsMap = new HashMap<>();
        Map<Integer, RecommendationItemDTO> serieRecsMap = new HashMap<>();

        for (int i = 0; i < Math.min(3, shuffledMovies.size()); i++) {
            List<RecommendationItemDTO> recs = fetchMovieRecommendations(shuffledMovies.get(i).getMovie().getId(), existingMovieIds);
            for(RecommendationItemDTO rec : recs) {
                movieRecsMap.putIfAbsent(rec.getId(), rec);
            }
        }

        for (int i = 0; i < Math.min(3, shuffledSeries.size()); i++) {
            List<RecommendationItemDTO> recs = fetchSerieRecommendations(shuffledSeries.get(i).getSerie().getId(), existingSerieIds);
            for(RecommendationItemDTO rec : recs) {
                serieRecsMap.putIfAbsent(rec.getId(), rec);
            }
        }

        List<RecommendationItemDTO> finalMovieRecs = new ArrayList<>(movieRecsMap.values());
        Collections.shuffle(finalMovieRecs); 
        
        List<RecommendationItemDTO> finalSerieRecs = new ArrayList<>(serieRecsMap.values());
        Collections.shuffle(finalSerieRecs); 

        List<RecommendationItemDTO> mixedResults = new ArrayList<>();
        int maxSize = Math.max(finalMovieRecs.size(), finalSerieRecs.size());
        
        for (int i = 0; i < maxSize; i++) {
            if (i < finalMovieRecs.size()) {
                mixedResults.add(finalMovieRecs.get(i));
            }
            if (i < finalSerieRecs.size()) {
                mixedResults.add(finalSerieRecs.get(i));
            }
            if (mixedResults.size() >= 20) {
                break;
            }
        }

        // 3. On sauvegarde dans le cache avant de retourner
        recommendationCache.put(userEmail, new CacheEntry(mixedResults, today));

        return mixedResults;
    }

    private List<RecommendationItemDTO> fetchMovieRecommendations(int movieId, Set<Integer> existingIds) {
        try {
            Map<String, Object> response = tmdbService.getMovieRecommendationsMap(movieId);
            if (response.get("results") instanceof List<?> rawResults) {
                return rawResults.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .filter(m -> {
                        Integer id = (Integer) m.get("id");
                        return id != null && !existingIds.contains(id);
                    })
                    .limit(10)
                    .map(m -> RecommendationItemDTO.builder()
                        .id((Integer) m.get("id"))
                        .title((String) m.get("title"))
                        .posterPath((String) m.get("poster_path"))
                        .voteAverage(m.get("vote_average") != null ? ((Number) m.get("vote_average")).doubleValue() : 0.0)
                        .releaseDate((String) m.get("release_date"))
                        .type("movie")
                        .build())
                    .toList();
            }
        } catch (Exception e) {
            // Ignorer
        }
        return Collections.emptyList();
    }

    private List<RecommendationItemDTO> fetchSerieRecommendations(int serieId, Set<Integer> existingIds) {
        try {
            Map<String, Object> response = tmdbService.getSerieRecommendationsMap(serieId);
            if (response.get("results") instanceof List<?> rawResults) {
                return rawResults.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .filter(s -> {
                        Integer id = (Integer) s.get("id");
                        return id != null && !existingIds.contains(id);
                    })
                    .limit(10)
                    .map(s -> RecommendationItemDTO.builder()
                        .id((Integer) s.get("id"))
                        .title((String) s.get("name"))
                        .posterPath((String) s.get("poster_path"))
                        .voteAverage(s.get("vote_average") != null ? ((Number) s.get("vote_average")).doubleValue() : 0.0)
                        .releaseDate((String) s.get("first_air_date"))
                        .type("serie")
                        .build())
                    .toList();
            }
        } catch (Exception e) {
            // Ignorer
        }
        return Collections.emptyList();
    }
}
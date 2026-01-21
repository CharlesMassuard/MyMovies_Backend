package fr.charlesmassuard.mymovies_api.service;

import org.springframework.stereotype.Service;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;

import fr.charlesmassuard.mymovies_api.model.Movie;
import fr.charlesmassuard.mymovies_api.model.Status;
import fr.charlesmassuard.mymovies_api.model.User;
import fr.charlesmassuard.mymovies_api.model.UserMovie;
import fr.charlesmassuard.mymovies_api.repository.MovieRepository;
import fr.charlesmassuard.mymovies_api.repository.UserMovieRepository;
import fr.charlesmassuard.mymovies_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserMovieService {

    private final UserMovieRepository userMovieRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;

    public void addToWatchlist(String userEmail, int movieId) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Movie movie = movieRepository.findById(movieId)
            .orElseGet(() -> {
                Map<String, Object> data = tmdbService.getMovieDetailsMap(movieId);
                
                Movie movieFromTmdb = Movie.builder()
                    .id(movieId)
                    .title((String) data.get("title"))
                    .releaseDate(LocalDate.parse((String) data.get("release_date")))
                    .resume((String) data.get("overview"))
                    .posterUrl((String) data.get("poster_path"))
                    .duration((Integer) data.get("runtime"))
                    .addedBDDate(LocalDateTime.now())
                    .rate(0)
                    .actors(new java.util.HashSet<fr.charlesmassuard.mymovies_api.model.Actor>())
                    .directors(new java.util.HashSet<fr.charlesmassuard.mymovies_api.model.Director>())
                    .types(new java.util.HashSet<fr.charlesmassuard.mymovies_api.model.Type>())
                    .build();
                    
                return movieRepository.save(movieFromTmdb);
            });
        
        UserMovie userMovie = UserMovie.builder()
            .commentaire(null)
            .rating(-1)
            .isPublic(false)
            .dateAdded(LocalDateTime.now())
            .status(Status.TO_WATCH)
            .user(user)
            .movie(movie)
            .build();
        
        userMovieRepository.save(userMovie);
    }

    public String getUserMovieStatus(String userEmail, int movieId) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return userMovieRepository.findByUserIdAndMovieId(user.getId(), movieId)
            .map(um -> {
                System.out.println("Status found: " + um.getStatus().name());
                return um.getStatus().name();
            })
            .orElse("UNDEFINED");
    }
}
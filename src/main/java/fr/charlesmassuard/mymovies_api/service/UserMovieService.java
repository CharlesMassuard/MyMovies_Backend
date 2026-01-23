package fr.charlesmassuard.mymovies_api.service;

import org.springframework.stereotype.Service;

import fr.charlesmassuard.mymovies_api.dto.MovieDTO;
import fr.charlesmassuard.mymovies_api.dto.UserMovieResponseDTO;
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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserMovieService {

    private final UserMovieRepository userMovieRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final TmdbService tmdbService;

    private static final String USER_NOT_FOUND = "Utilisateur non trouvé";
    private static final String UNDEFINED = "UNDEFINED";

    public void addToWatchlist(String userEmail, int movieId) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        
        Movie movie = getOrCreateMovie(movieId);
        
        UserMovie userMovie = userMovieRepository.findByUserIdAndMovieId(user.getId(), movieId)
            .orElseGet(() -> UserMovie.builder()
                .user(user)
                .movie(movie)
                .dateAdded(LocalDateTime.now())
                .build());

        userMovie.setStatus(Status.TO_WATCH);
        userMovieRepository.save(userMovie);
    }

    public void rateUserMovie(String userEmail, int movieId, int rating, String comment) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        
        Movie movie = getOrCreateMovie(movieId);

        UserMovie userMovie = userMovieRepository.findByUserIdAndMovieId(user.getId(), movieId)
            .orElseGet(() -> UserMovie.builder()
                .user(user)
                .movie(movie)
                .status(Status.WATCHED)
                .dateAdded(LocalDateTime.now())
                .dateViewed(LocalDateTime.now())
                .build());
        
        userMovie.setRating(rating);
        userMovie.setCommentaire(comment);
        userMovie.setStatus(Status.WATCHED);
        if(userMovie.getDateViewed() == null){
            userMovie.setDateViewed(LocalDateTime.now());
        }
        userMovieRepository.save(userMovie);
    }

    private Movie getOrCreateMovie(int movieId) {
        return movieRepository.findById(movieId)
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
                    .actors(new java.util.HashSet<>())
                    .directors(new java.util.HashSet<>())
                    .types(new java.util.HashSet<>())
                    .build();
                return movieRepository.save(movieFromTmdb);
            });
    }

    public String getUserMovieStatus(String userEmail, int movieId) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        return userMovieRepository.findByUserIdAndMovieId(user.getId(), movieId)
            .map(um -> um.getStatus().name())
            .orElse(UNDEFINED);
    }

    public String getUserMovieWatchedDate(String userEmail, int movieId) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        
        return userMovieRepository.findByUserIdAndMovieId(user.getId(), movieId)
            .map(um -> {
                LocalDateTime dateViewed = um.getDateViewed();
                LocalDate dateOnly = dateViewed != null ? dateViewed.toLocalDate() : null;
                return dateOnly != null ? dateOnly.toString() : "NOT_WATCHED_YET";
            })
            .orElse(UNDEFINED);
    }

    public Integer getUserMovieRating(String userEmail, int movieId) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
            
        return userMovieRepository.findByUserIdAndMovieId(user.getId(), movieId)
            .map(UserMovie::getRating)
            .orElse(-1);
    }

    public String getUserMovieComment(String userEmail, int movieId) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        
        return userMovieRepository.findByUserIdAndMovieId(user.getId(), movieId)
            .map(um -> um.getCommentaire() != null ? um.getCommentaire() : "NO_COMMENT")
            .orElse(UNDEFINED);
    }

    public void updateUserMovieStatus(String userEmail, int movieId, String statusStr, String watchedAtStr) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        
        Status status;
        try {
            status = Status.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut invalide", e);
        }
        
        UserMovie userMovie = userMovieRepository.findByUserIdAndMovieId(user.getId(), movieId)
            .orElseThrow(() -> new RuntimeException("Film non trouvé dans la liste de l'utilisateur"));
        
        userMovie.setStatus(status);
        if(status == Status.WATCHED) {
            if (watchedAtStr != null && !watchedAtStr.isEmpty()) {
                LocalDate watchedAt = LocalDate.parse(watchedAtStr);
                userMovie.setDateViewed(watchedAt.atStartOfDay());
            } else if (userMovie.getDateViewed() == null) {
                userMovie.setDateViewed(LocalDateTime.now());
            }
        }
        userMovieRepository.save(userMovie);
    }

    public void deleteUserMovieStatus(String userEmail, int movieId) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        
        UserMovie userMovie = userMovieRepository.findByUserIdAndMovieId(user.getId(), movieId)
            .orElseThrow(() -> new RuntimeException("Film non trouvé dans la liste de l'utilisateur"));
        
        userMovieRepository.delete(userMovie);
    }

    public List<UserMovieResponseDTO> getUserMovies(String userEmail) {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        
        List<UserMovie> userMovies = userMovieRepository.findAllByUserId(user.getId());
        
        return userMovies.stream().map(um -> {
            Movie movie = um.getMovie();
            MovieDTO movieDTO = MovieDTO.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .releaseDate(movie.getReleaseDate())
                .resume(movie.getResume())
                .posterUrl(movie.getPosterUrl())
                .duration(movie.getDuration())
                .addedBDDate(movie.getAddedBDDate())
                .rate(movie.getRate())
                .build();
            return UserMovieResponseDTO.builder()
                .rating(um.getRating())
                .status(um.getStatus())
                .movie(movieDTO)
                .build();
        }).toList();
    }
}
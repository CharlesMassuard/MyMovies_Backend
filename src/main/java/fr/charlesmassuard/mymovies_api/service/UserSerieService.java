package fr.charlesmassuard.mymovies_api.service;

import org.springframework.stereotype.Service;

import fr.charlesmassuard.mymovies_api.dto.SerieDTO;
import fr.charlesmassuard.mymovies_api.dto.UserSerieResponseDTO;
import fr.charlesmassuard.mymovies_api.exceptions.UserException;
import fr.charlesmassuard.mymovies_api.model.Serie;
import fr.charlesmassuard.mymovies_api.model.Status;
import fr.charlesmassuard.mymovies_api.model.User;
import fr.charlesmassuard.mymovies_api.model.UserSerie;
import fr.charlesmassuard.mymovies_api.repository.SerieRepository;
import fr.charlesmassuard.mymovies_api.repository.UserSerieRepository;
import fr.charlesmassuard.mymovies_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserSerieService {

    private final UserSerieRepository userSerieRepository;
    private final UserRepository userRepository;
    private final SerieRepository serieRepository;
    private final TmdbService tmdbService;

    private static final String USER_NOT_FOUND = "Utilisateur non trouvé";
    private static final String UNDEFINED = "UNDEFINED";

    public void addToWatchlist(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        
        Serie serie = getOrCreateSerie(serieId);
        
        UserSerie userSerie = userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .orElseGet(() -> UserSerie.builder()
                .user(user)
                .serie(serie)
                .dateAdded(LocalDateTime.now())
                .build());

        userSerie.setStatus(Status.TO_WATCH);
        userSerieRepository.save(userSerie);
    }

    public void rateUserSerie(String userEmail, int serieId, int rating, String comment) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        
        Serie serie = getOrCreateSerie(serieId);

        UserSerie userSerie = userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .orElseGet(() -> UserSerie.builder()
                .user(user)
                .serie(serie)
                .status(Status.WATCHED)
                .dateAdded(LocalDateTime.now())
                .dateViewed(LocalDateTime.now())
                .build());
        
        userSerie.setRating(rating);
        userSerie.setCommentaire(comment);
        userSerie.setStatus(Status.WATCHED);
        if(userSerie.getDateViewed() == null){
            userSerie.setDateViewed(LocalDateTime.now());
        }
        userSerieRepository.save(userSerie);
    }

    private Serie getOrCreateSerie(int serieId) {
        return serieRepository.findById(serieId)
            .orElseGet(() -> {
                Map<String, Object> data = tmdbService.getSerieDetailsMap(serieId);
                
                //Gestion des dates de séries
                String airDateStr = (String) data.get("first_air_date");
                LocalDate airDate = (airDateStr != null && !airDateStr.isBlank()) ? LocalDate.parse(airDateStr) : null;
                
                //La durée d'un épisode est un tableau sur les séries
                int runtime = 0;
                if (data.get("episode_run_time") instanceof List<?> runtimes && !runtimes.isEmpty()) {
                    runtime = ((Number) runtimes.get(0)).intValue();
                }

                Serie serieFromTmdb = Serie.builder()
                    .id(serieId)
                    .title((String) data.get("name")) //TMDB Series = name
                    .releaseDate(airDate)
                    .resume((String) data.get("overview"))
                    .posterUrl((String) data.get("poster_path"))
                    .duration(runtime) 
                    .addedBDDate(LocalDateTime.now())
                    .rate(0)
                    .actors(new java.util.HashSet<>())
                    .directors(new java.util.HashSet<>())
                    .types(new java.util.HashSet<>())
                    .build();
                return serieRepository.save(serieFromTmdb);
            });
    }

    public String getUserSerieStatus(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        return userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .map(us -> us.getStatus().name())
            .orElse(UNDEFINED);
    }

    public String getUserSerieWatchedDate(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        
        return userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .map(us -> {
                LocalDateTime dateViewed = us.getDateViewed();
                LocalDate dateOnly = dateViewed != null ? dateViewed.toLocalDate() : null;
                return dateOnly != null ? dateOnly.toString() : "NOT_WATCHED_YET";
            })
            .orElse(UNDEFINED);
    }

    public Integer getUserSerieRating(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));
            
        return userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .map(UserSerie::getRating)
            .orElse(-1);
    }

    public String getUserSerieComment(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        
        return userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .map(us -> us.getCommentaire() != null ? us.getCommentaire() : "NO_COMMENT")
            .orElse(UNDEFINED);
    }

    public void updateUserSerieStatus(String userEmail, int serieId, String statusStr, String watchedAtStr) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        
        Status status;
        try {
            status = Status.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Statut invalide", e);
        }
        
        UserSerie userSerie = userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .orElseThrow(() -> new UserException("Série non trouvée dans la liste de l'utilisateur"));
        
        userSerie.setStatus(status);
        if(status == Status.WATCHED) {
            if (watchedAtStr != null && !watchedAtStr.isEmpty()) {
                LocalDate watchedAt = LocalDate.parse(watchedAtStr);
                userSerie.setDateViewed(watchedAt.atStartOfDay());
            } else if (userSerie.getDateViewed() == null) {
                userSerie.setDateViewed(LocalDateTime.now());
            }
        }
        userSerieRepository.save(userSerie);
    }

    public void deleteUserSerieStatus(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        
        UserSerie userSerie = userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .orElseThrow(() -> new UserException("Série non trouvée dans la liste de l'utilisateur"));
        
        userSerieRepository.delete(userSerie);
    }

    public List<UserSerieResponseDTO> getUserSeries(String userEmail) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(USER_NOT_FOUND));
        
        List<UserSerie> userSeries = userSerieRepository.findAllByUserId(user.getId());
        
        return userSeries.stream().map(us -> {
            Serie serie = us.getSerie();
            SerieDTO serieDTO = SerieDTO.builder()
                .id(serie.getId())
                .title(serie.getTitle())
                .releaseDate(serie.getReleaseDate())
                .resume(serie.getResume())
                .posterUrl(serie.getPosterUrl())
                .duration(serie.getDuration())
                .addedBDDate(serie.getAddedBDDate())
                .rate(serie.getRate())
                .build();
            return UserSerieResponseDTO.builder()
                .rating(us.getRating())
                .status(us.getStatus())
                .serie(serieDTO)
                .build();
        }).toList();
    }
}
package fr.charlesmassuard.mymovies_api.service;

import org.springframework.stereotype.Service;

import fr.charlesmassuard.mymovies_api.dto.SerieDTO;
import fr.charlesmassuard.mymovies_api.dto.UserSerieResponseDTO;
import fr.charlesmassuard.mymovies_api.exceptions.UserException;
import fr.charlesmassuard.mymovies_api.model.Serie;
import fr.charlesmassuard.mymovies_api.model.Status;
import fr.charlesmassuard.mymovies_api.model.User;
import fr.charlesmassuard.mymovies_api.model.UserMovie;
import fr.charlesmassuard.mymovies_api.model.UserSerie;
import fr.charlesmassuard.mymovies_api.repository.SerieRepository;
import fr.charlesmassuard.mymovies_api.repository.UserSerieRepository;
import fr.charlesmassuard.mymovies_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import fr.charlesmassuard.mymovies_api.dto.WatchHistoryDTO;
import fr.charlesmassuard.mymovies_api.model.SerieWatchHistory;
import java.util.ArrayList;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class UserSerieService {

    private final UserSerieRepository userSerieRepository;
    private final UserRepository userRepository;
    private final SerieRepository serieRepository;
    private final TmdbService tmdbService;
    private final I18nService i18n;
    private static final String UNDEFINED = "UNDEFINED";

    public void addToWatchlist(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        
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
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        
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
                
                String airDateStr = (String) data.get("first_air_date");
                LocalDate airDate = (airDateStr != null && !airDateStr.isBlank()) ? LocalDate.parse(airDateStr) : null;
                
                int runtime = 0;
                if (data.get("episode_run_time") instanceof List<?> runtimes && !runtimes.isEmpty()) {
                    runtime = ((Number) runtimes.get(0)).intValue();
                }

                Serie serieFromTmdb = Serie.builder()
                    .id(serieId)
                    .title((String) data.get("name"))
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
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        return userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .map(us -> us.getStatus().name())
            .orElse(UNDEFINED);
    }

    public String getUserSerieWatchedDate(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        
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
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
            
        return userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .map(UserSerie::getRating)
            .orElse(-1);
    }

    public String getUserSerieComment(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        
        return userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .map(us -> us.getCommentaire() != null ? us.getCommentaire() : "NO_COMMENT")
            .orElse(UNDEFINED);
    }

    public void updateUserSerieStatus(String userEmail, int serieId, String statusStr, String watchedAtStr) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        
        Status status;
        try {
            status = Status.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(i18n.get("error.invalidStatus"), e);
        }
        
        UserSerie userSerie = userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .orElseThrow(() -> new UserException(i18n.get("error.seriesNotFound")));
        
        userSerie.setStatus(status);
        
        if(status == Status.WATCHED) {
            if (watchedAtStr != null && !watchedAtStr.isEmpty()) {
                LocalDate watchedAt = LocalDate.parse(watchedAtStr);
                
                // CORRECTION ICI : Si c'est aujourd'hui, on met l'heure exacte
                if (watchedAt.equals(LocalDate.now())) {
                    userSerie.setDateViewed(LocalDateTime.now());
                } else {
                    userSerie.setDateViewed(watchedAt.atTime(12, 0)); // Midi par défaut
                }
            } else if (userSerie.getDateViewed() == null) {
                userSerie.setDateViewed(LocalDateTime.now());
            }
        } else {
            // Optionnel : Mettre à jour la date d'ajout si le statut passe à "En cours"
            userSerie.setDateAdded(LocalDateTime.now());
        }
        
        userSerieRepository.save(userSerie);
    }

    public void deleteUserSerieStatus(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        
        UserSerie userSerie = userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .orElseThrow(() -> new UserException(i18n.get("error.seriesNotFound")));
        
        userSerieRepository.delete(userSerie);
    }

    public List<UserSerieResponseDTO> getUserSeries(String userEmail) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        
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
                .dateAdded(us.getDateAdded())
                .dateViewed(us.getDateViewed())
                .build();
        }).toList();
    }

    public List<WatchHistoryDTO> getSerieWatchHistory(String userEmail, int serieId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        
        UserSerie userSerie = userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .orElseThrow(() -> new UserException(i18n.get("error.seriesNotFound")));

        List<WatchHistoryDTO> historyList = new ArrayList<>();

        //On ajoute le visionnage historique principal (celui déjà en base)
        if (userSerie.getStatus() == Status.WATCHED) {
            historyList.add(WatchHistoryDTO.builder()
                .dateViewed(userSerie.getDateViewed())
                .rating(userSerie.getRating())
                .comment(userSerie.getCommentaire())
                .isMain(true)
                .build());
        }

        //On ajoute les visionnages successifs
        if (userSerie.getWatchHistories() != null) {
            for (SerieWatchHistory history : userSerie.getWatchHistories()) {
                historyList.add(WatchHistoryDTO.builder()
                    .id(history.getId())
                    .dateViewed(history.getDateViewed())
                    .rating(history.getRating())
                    .comment(history.getCommentaire())
                    .isMain(false)
                    .build());
            }
        }

        //Tri par date décroissante (du plus récent au plus ancien)
        historyList.sort(Comparator.comparing(WatchHistoryDTO::getDateViewed, Comparator.nullsLast(Comparator.reverseOrder())));
        
        return historyList;
    }

    public void addSerieRewatch(String userEmail, int serieId, String dateViewedStr, int rating, String comment) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        
        UserSerie userSerie = userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .orElseThrow(() -> new UserException(i18n.get("error.seriesNotFound")));

        LocalDateTime viewDate = (dateViewedStr != null && !dateViewedStr.isEmpty()) 
            ? LocalDate.parse(dateViewedStr).atTime(12, 0) 
            : LocalDateTime.now();

        SerieWatchHistory newHistory = SerieWatchHistory.builder()
            .dateViewed(viewDate)
            .rating(rating)
            .commentaire(comment)
            .userSerie(userSerie)
            .build();

        if (userSerie.getWatchHistories() == null) {
            userSerie.setWatchHistories(new ArrayList<>());
        }
        userSerie.getWatchHistories().add(newHistory);
        
        userSerieRepository.save(userSerie);
    }

    public void deleteSerieRewatch(String userEmail, int serieId, int historyId) throws UserException {
        User user = userRepository.findByMail(userEmail)
            .orElseThrow(() -> new UserException(i18n.get("error.userNotFound")));
        
        UserSerie userSerie = userSerieRepository.findByUserIdAndSerieId(user.getId(), serieId)
            .orElseThrow(() -> new UserException(i18n.get("error.seriesNotFound")));

        if (userSerie.getWatchHistories() != null) {
            userSerie.getWatchHistories().removeIf(h -> h.getId() == historyId);
            userSerieRepository.save(userSerie);
        }
    }
}

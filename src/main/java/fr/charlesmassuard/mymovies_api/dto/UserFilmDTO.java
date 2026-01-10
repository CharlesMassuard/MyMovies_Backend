package fr.charlesmassuard.mymovies_api.dto;

import java.time.LocalDateTime;

import fr.charlesmassuard.mymovies_api.model.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserFilmDTO {

    private String commentaire;
    private int rating;
    private boolean isPublic;
    private LocalDateTime dateAdded;
    private Status status;
    private LocalDateTime dateViewed;
    private UserDTO user;
    private MovieDTO film;
}

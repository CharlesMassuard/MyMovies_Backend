package fr.charlesmassuard.mymovies_api.dto;

import java.time.LocalDateTime;

import fr.charlesmassuard.mymovies_api.model.Status;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserMovieDTO {

    private String commentaire;
    private int rating;
    private boolean isPublic;
    private LocalDateTime dateAdded;
    private Status status;
    private LocalDateTime dateViewed;
    private UserDTO user;
    private MovieDTO film;
}

package fr.charlesmassuard.mymovies_api.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import fr.charlesmassuard.mymovies_api.model.Status;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserSerieResponseDTO {
    
    private int rating;
    private Status status;
    private SerieDTO serie;
    private LocalDateTime dateAdded;
    private LocalDateTime dateViewed;
}
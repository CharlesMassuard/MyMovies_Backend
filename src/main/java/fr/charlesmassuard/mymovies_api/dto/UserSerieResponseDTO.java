package fr.charlesmassuard.mymovies_api.dto;

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
    //Remplacement de movie par serie
    private SerieDTO serie;
}
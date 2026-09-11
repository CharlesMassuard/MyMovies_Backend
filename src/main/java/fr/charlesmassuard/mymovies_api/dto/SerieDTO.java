package fr.charlesmassuard.mymovies_api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
public class SerieDTO {
    
    private int id;
    private String title;
    private LocalDate releaseDate;
    private String resume;
    private String posterUrl;
    private int duration;
    private LocalDateTime addedBDDate;
    private int rate;
}
package fr.charlesmassuard.mymovies_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationItemDTO {
    private int id;
    private String title;
    private String posterPath;
    private double voteAverage;
    private String releaseDate;
    private String type; //"movie"-ou-"serie"
}
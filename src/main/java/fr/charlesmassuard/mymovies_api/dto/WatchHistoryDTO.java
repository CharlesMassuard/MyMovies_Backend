package fr.charlesmassuard.mymovies_api.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WatchHistoryDTO {
    private Integer id;
    private LocalDateTime dateViewed;
    private int rating;
    private String comment;
    private boolean isMain; //Permet au front de savoir si c'est le visionnage principal
}
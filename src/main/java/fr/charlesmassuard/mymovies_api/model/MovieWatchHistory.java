package fr.charlesmassuard.mymovies_api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "movie_watch_history")
public class MovieWatchHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private int id;

    @Column(name = "date_viewed")
    private LocalDateTime dateViewed;

    @Column(name = "rating")
    private int rating;

    @Column(name = "commentaire", columnDefinition = "TEXT")
    private String commentaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_movie_id", nullable = false)
    private UserMovie userMovie;
}
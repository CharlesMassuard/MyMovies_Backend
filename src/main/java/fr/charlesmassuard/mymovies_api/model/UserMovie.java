package fr.charlesmassuard.mymovies_api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "user_movie")
public class UserMovie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_movie_id")
    private int id;

    @Column(name = "user_movie_commentaire")
    private String commentaire;

    @Column(name = "user_movie_rating")
    private int rating;

    @Column(name = "user_movie_is_public")
    private boolean isPublic;

    @Column(name = "user_movie_date_added")
    private LocalDateTime dateAdded;
    
    @Column(name = "user_movie_status")
    private Status status;

    @Column(name = "user_movie_date_viewed")
    private LocalDateTime dateViewed;

    //User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    //Movie
    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
}

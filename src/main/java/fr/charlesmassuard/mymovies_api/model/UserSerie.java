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
@Table(name = "user_serie")
public class UserSerie {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_serie_id")
    private int id;

    @Column(name = "user_serie_commentaire")
    private String commentaire;

    @Column(name = "user_serie_rating")
    private int rating;

    @Column(name = "user_serie_is_public")
    private boolean isPublic;

    @Column(name = "user_serie_date_added")
    private LocalDateTime dateAdded;
    
    @Column(name = "user_serie_status")
    private Status status;

    @Column(name = "user_serie_date_viewed")
    private LocalDateTime dateViewed;

    //User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    //Serie
    @ManyToOne
    @JoinColumn(name = "serie_id", nullable = false)
    private Serie serie;
}

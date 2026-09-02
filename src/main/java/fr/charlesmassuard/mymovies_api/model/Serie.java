package fr.charlesmassuard.mymovies_api.model;

import jakarta.persistence.*;
import java.time.LocalDate;
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
@Table(name = "serie")
public class Serie {
    
    @Id
    @Column(name = "serie_id")
    private int id;

    @Column(nullable = false)
    private String title;

    private LocalDate releaseDate;

    @Column(columnDefinition = "TEXT")
    private String resume;

    private String posterUrl;

    private int duration; //en minutes

    @Column(nullable = false)
    private LocalDateTime addedBDDate;

    private int rate;

    //Actors
    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "serie_actors",
        joinColumns = @JoinColumn(name = "serie_id"),
        inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    private java.util.Set<Actor> actors = new java.util.HashSet<>();
    
    //Directors
    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "serie_directors",
        joinColumns = @JoinColumn(name = "serie_id"),
        inverseJoinColumns = @JoinColumn(name = "director_id")
    )
    private java.util.Set<Director> directors = new java.util.HashSet<>();

    //Types
    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "serie_types",
        joinColumns = @JoinColumn(name = "serie_id"),
        inverseJoinColumns = @JoinColumn(name = "type_id")
    )
    private java.util.Set<Type> types = new java.util.HashSet<>();
}
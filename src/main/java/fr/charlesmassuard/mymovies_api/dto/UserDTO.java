package fr.charlesmassuard.mymovies_api.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserDTO {

    private String pseudo;
    private String mail;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;
    
}

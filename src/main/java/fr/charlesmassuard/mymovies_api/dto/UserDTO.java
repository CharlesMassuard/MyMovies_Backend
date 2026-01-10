package fr.charlesmassuard.mymovies_api.dto;

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
public class UserDTO {

    private String pseudo;
    private String mail;
    private String password;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;
    
}

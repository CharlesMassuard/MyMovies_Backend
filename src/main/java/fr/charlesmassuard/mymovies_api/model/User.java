package fr.charlesmassuard.mymovies_api.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String pseudo;
    private String mail;
    private String password;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLoginDate;

    public User() {}

    public User(String pseudo, String mail, String password, LocalDateTime registrationDate, LocalDateTime lastLoginDate) {
        this.pseudo = pseudo;
        this.mail = mail;
        this.password = password;
        this.registrationDate = registrationDate;
        this.lastLoginDate = lastLoginDate;
    }

    public int getId() {
        return id;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
}

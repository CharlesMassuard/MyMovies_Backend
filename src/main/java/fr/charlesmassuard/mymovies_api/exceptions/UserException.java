package fr.charlesmassuard.mymovies_api.exceptions;

public class UserException extends RuntimeException { 
    public UserException(String errorMessage) {
        super(errorMessage);
    }
}

package fr.charlesmassuard.mymovies_api.exceptions;

public class UserException extends Exception { 
    public UserException(String errorMessage) {
        super(errorMessage);
    }
}

package com.just.takeaway.exception;

public class DuplicateGameIdException extends RuntimeException {
    public DuplicateGameIdException(String s) {
        super(s);
    }
}

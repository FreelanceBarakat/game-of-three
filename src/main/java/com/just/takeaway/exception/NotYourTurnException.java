package com.just.takeaway.exception;

public class NotYourTurnException extends RuntimeException {
    public NotYourTurnException(String s) {
        super(s);
    }
}

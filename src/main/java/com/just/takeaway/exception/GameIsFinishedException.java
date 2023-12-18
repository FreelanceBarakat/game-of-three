package com.just.takeaway.exception;

public class GameIsFinishedException  extends RuntimeException {
    public GameIsFinishedException(String s) {
        super(s);
    }
}

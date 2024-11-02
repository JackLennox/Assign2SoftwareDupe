package com.example.musicFinder;

public class LyricsNotFoundException extends RuntimeException {
    public LyricsNotFoundException(String message) {
        super(message);
    }
}

package com.example.musicFinder.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.musicFinder.controller.MusicFinderController;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class SongControllerTest {
    private MusicFinderController musicFinderController;
    private RestTemplate restTemplate;
    private static final String API_URL = "https://api.lyrics.ovh/v1/";

    @BeforeEach
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        musicFinderController = new MusicFinderController(restTemplate);
    }

    @Test
    public void testFetchLyrics_ValidSong() {
        // Arrange: Mock a successful API response
        String artist = "Sample Artist";
        String song = "Sample Song";
        String mockApiResponse = "{\"lyrics\":\"Sample lyrics\\nfor a song.\"}";

        when(restTemplate.getForObject(API_URL + artist + "/" + song, String.class))
            .thenReturn(mockApiResponse);
        
        // Act: Call the controller
        ResponseEntity<ObjectNode> response = musicFinderController.getSongDetails(artist, song);

        // Assert: 200 OK, and verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ObjectNode body = response.getBody();
        assertEquals(artist, body.get("artist").asText());
        assertEquals(song, body.get("song").asText());
        assertEquals("Sample lyrics<br>for a song.", body.get("lyrics").asText());
        assertEquals("https://www.youtube.com/results?search_query=Sample+Artist+Sample+Song", body.get("youtubeSearch").asText());
    }

    @Test
    public void testFetchLyrics_LongNames() {
        // Arrange: Mock a successful API response
        String artist = "A".repeat(1000);
        String song = "B".repeat(1000);
        String mockApiResponse = "{\"lyrics\":\"Sample lyrics\\nfor a song.\"}";

        when(restTemplate.getForObject(API_URL + artist + "/" + song, String.class))
            .thenReturn(mockApiResponse);
        
        // Act: Call the controller
        ResponseEntity<ObjectNode> response = musicFinderController.getSongDetails(artist, song);

        // Assert: 200 OK, and verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ObjectNode body = response.getBody();
        assertEquals(artist, body.get("artist").asText());
        assertEquals(song, body.get("song").asText());
        assertEquals("Sample lyrics<br>for a song.", body.get("lyrics").asText());
        assertEquals(
            "https://www.youtube.com/results?search_query=" + artist.replace(" ", "+") + "+" + song.replace(" ", "+"), 
            body.get("youtubeSearch").asText()
        ); 
    }

    @Test
    public void testFetchLyrics_UnknownSong() {
        // Arrange: Mock a failed API response
        String artist = "Unknown Artist";
        String song = "Unknown Song";
        String mockApiResponse = "{\"error\":\"No lyrics found\"}";
        
        when(restTemplate.getForObject(API_URL + artist + "/" + song, String.class))
            .thenReturn(mockApiResponse);

        // Act: Call the controller
        ResponseEntity<ObjectNode> response = musicFinderController.getSongDetails(artist, song);

        // Assert: 404 Not Found, and verify error message
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Lyrics not found for " + artist + " - " + song, response.getBody().get("error").asText());
    }

    @Test
    public void testFetchLyrics_NonJsonResponse() {
        // Arrange: Mock a non-JSON API response
        String artist = "Sample Artist";
        String song = "Sample song";
        String mockApiResponse = "Not a JSON response";

        when(restTemplate.getForObject(API_URL + artist + "/" + song, String.class))
            .thenReturn(mockApiResponse);
        
        // Act: Call the controller
        ResponseEntity<ObjectNode> response = musicFinderController.getSongDetails(artist, song);

        // Assert: 404 Not Found, and verify error message
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Lyrics not found for " + artist + " - " + song, response.getBody().get("error").asText());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    public void testFetchLyrics_EmptyAndNullInput(String input) {
        // Act: Call the controller with empty strings
        ResponseEntity<ObjectNode> response = musicFinderController.getSongDetails(input, input);

        // Assert: 400 Bad Request, and verify error message
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Artist and song must not be null or empty", response.getBody().get("error").asText());
    }
}

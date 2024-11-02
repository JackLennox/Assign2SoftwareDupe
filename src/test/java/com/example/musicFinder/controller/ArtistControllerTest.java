package com.example.musicFinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArtistControllerTest {
    private ArtistController artistController;
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        artistController = new ArtistController(restTemplate);
    }

    @Test
    public void testFetchInformation_ValidArtist() {
        String artist = "Sample Artist";

        // Mock the Wikipedia search API response
        String searchResponse = "[\"" + artist + "\", [\"" + artist + " (musician)\"], [], []]";
        when(restTemplate.getForObject("https://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=" + artist, String.class))
                .thenReturn(searchResponse);

        // Mock the Wikipedia response for the selected artist page
        String summaryResponse = "{ \"extract\": \"Sample information for an artist.\" }";
        when(restTemplate.getForObject("https://en.wikipedia.org/api/rest_v1/page/summary/Sample Artist (musician)", String.class))
                .thenReturn(summaryResponse);

        // Act: Call the controller
        ResponseEntity<ObjectNode> response = artistController.getArtistInformation(artist);

        // Assert: 200 OK, and verify response
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Expected status 200 OK");

        ObjectNode body = response.getBody();
        assertEquals(artist, body.get("artist").asText());
        assertEquals("Sample information for an artist.\n<a href='https://en.wikipedia.org/wiki/Sample Artist (musician)'>Wikipedia Page</a>", body.get("info").asText());
    }



    @Test
    public void testFetchInformation_ArtistNotFound() {
        String artist = "NonExistentArtist";

        // Mock the Wikipedia search API response to simulate no articles found
        String searchResponse = "[\"" + artist + "\", [], [], []]";
        when(restTemplate.getForObject("https://en.wikipedia.org/w/api.php?action=opensearch&format=json&search=" + artist, String.class))
                .thenReturn(searchResponse);

        // Act: Call the controller
        ResponseEntity<ObjectNode> response = artistController.getArtistInformation(artist);

        // Debugging print statements to check the response
        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());

        // Assert: 404 Not Found, and verify response
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Expected status 404 Not Found");

        ObjectNode body = response.getBody();
        assertEquals("Information for " + artist + " not found", body.get("error").asText());
    }


    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {""})
    public void testFetchInformation_EmptyArtist(String input) {
        // Act and Assert: Ensure a BAD_REQUEST status for empty or null artist names
        ResponseEntity<ObjectNode> response = artistController.getArtistInformation(input);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        ObjectNode body = response.getBody();
        assertEquals("Artist name cannot be null or empty", body.get("error").asText());
    }
}

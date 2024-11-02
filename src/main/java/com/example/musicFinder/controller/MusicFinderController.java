package com.example.musicFinder.controller;

import com.example.musicFinder.LyricsNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@RestController
public class MusicFinderController {

    // ObjectMapper to help with JSON formatting
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final RestTemplate restTemplate;

    // Constructor to inject RestTemplate
    public MusicFinderController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Basic status endpoint
    @GetMapping("/status")
    public String getStatus() {
        return "{\"status\":\"Application is running\"}";
    }

    // Fetch lyrics from Lyrics.ovh API and clean newline characters
    private String getFormattedLyrics(String artist, String song) {
        String apiUrl = "https://api.lyrics.ovh/v1/" + artist + "/" + song;
        try {
            // Fetch the raw JSON response
            String rawJson = restTemplate.getForObject(apiUrl, String.class);
    
            // Parse the JSON to extract the lyrics
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(rawJson);
            String rawLyrics = jsonNode.get("lyrics").asText();

            // Step 1: Remove carriage returns (\r)
            String formattedLyrics = rawLyrics.replace("\\r", "");
    
            // Step 2: Replace single newlines (\n) with a single <br>
            formattedLyrics = formattedLyrics.replace("\n", "<br>");
    
            // Step 3: Return the formatted lyrics
            return formattedLyrics.trim();
        } catch (Exception e) {
            throw new LyricsNotFoundException("Lyrics not found for " + artist + " - " + song);
        }
    }
    
    
    
    // Generate YouTube search link based on artist and song
    private String getYouTubeSearchUrl(String artist, String song) {
        String searchQuery = artist.replace(" ", "+") + "+" + song.replace(" ", "+");
        return "https://www.youtube.com/results?search_query=" + searchQuery;
    }

    // Fetch song details, YouTube search link, and formatted lyrics
    @GetMapping("/song/{artist}/{name}")
    public ResponseEntity<ObjectNode> getSongDetails(@PathVariable String artist, @PathVariable String name) {
        ObjectNode response = objectMapper.createObjectNode();

        // Validate input
        if (artist == null || name == null || artist.trim().isEmpty() || name.trim().isEmpty()) {
            // Return 400 Bad Request
            response.put("error", "Artist and song must not be null or empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Get the YouTube search link
        String youtubeSearchUrl = getYouTubeSearchUrl(artist, name);
        
        try {
            // Get the formatted lyrics
            String lyrics = getFormattedLyrics(artist, name);

            // Build a JSON response with the song and artist details
            response.put("song", name);
            response.put("artist", artist);
            response.put("youtubeSearch", youtubeSearchUrl);
            response.put("lyrics", lyrics);

            // Return 200 OK
            return ResponseEntity.ok(response);
        } catch (LyricsNotFoundException e) {
            // Return 404 Not Found
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}

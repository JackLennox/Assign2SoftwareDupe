package com.example.musicFinder.controller;

import com.example.musicFinder.ArtistNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ArtistController {

    // ObjectMapper to help with JSON formatting
    private final ObjectMapper mapper = new ObjectMapper();

    private final RestTemplate restTemplate;

    // Constructor to inject RestTemplate
    public ArtistController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    private String getFormattedArtistInformation(String artist) {
        // This is the string to get all the wiki articles with the input artist name
        String pageApiUrl = "https://en.wikipedia.org/w/api.php?action=opensearch&format=json&search="+artist;
        try {
            // Get all the pages for the given search term
            String rawJson = restTemplate.getForObject(pageApiUrl, String.class);
            JsonNode jsonNode = mapper.readTree(rawJson);

            // Convert the second element of the array (the article names) to a string array
            String[] pages = mapper.convertValue(jsonNode.get(1), String[].class);
            if(pages.length == 0){
                throw new ArtistNotFoundException("Information for " + artist + " not found");
            }

            //loop through the pages and find one that contains "artist", "band" or "musician" (if it exists)
            for(String page: pages){
                if(page.toLowerCase().contains("artist") || page.toLowerCase().contains("band") || page.toLowerCase().contains("musician")){
                    artist = page;
                    // Once it's found, we can continue. This is fine since the page are ranked in order of popularity
                    break;
                }
            }

            // If we have found an article with artist, band or musician in the title, we can use it to get the artist information
            // Otherwise we're going to use the original string query
            String apiUrl = "https://en.wikipedia.org/api/rest_v1/page/summary/"+artist;

            // Fetch the raw JSON response from the article
            String rawArticleJson = restTemplate.getForObject(apiUrl, String.class);

            // Parse the JSON to extract the artist information from the article
            ObjectMapper objectArticleMapper = new ObjectMapper();
            JsonNode jsonArticleNode = objectArticleMapper.readTree(rawArticleJson);
            String artistInfo = jsonArticleNode.get("extract").asText();

            artistInfo += "\n<a href='https://en.wikipedia.org/wiki/"+artist+"'>Wikipedia Page</a>";

            // Return the artist information
            return artistInfo.trim();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            throw new ArtistNotFoundException("Information for " + artist + " not found");
        }
    }

    // Fetch the artist information
    @GetMapping("/artist/{name}")
    public ResponseEntity<ObjectNode> getArtistInformation(@PathVariable String name) {
        ObjectNode response = mapper.createObjectNode();

        if(name == null || name.isEmpty()){
            response.put("error", "Artist name cannot be null or empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            // Fetch the artist information
            String artistInfo = getFormattedArtistInformation(name);

            // Add the artist information to the response
            response.put("artist", name);
            response.put("info", artistInfo);

            // Return the response
            return ResponseEntity.ok(response);
        } catch (ArtistNotFoundException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

}

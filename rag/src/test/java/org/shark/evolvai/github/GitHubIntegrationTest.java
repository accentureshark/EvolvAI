package org.shark.evolvai.github;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for GitHub API functionality
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GitHubIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testGetOrganizationRepositories() {
        // Test the GitHub API endpoint
        String url = "http://localhost:" + port + "/api/github/organizations/accentureshark/repositories";
        
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
        
        // Should return OK status
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // Should return a list
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
        
        // Print results for verification
        List<Object> repos = response.getBody();
        System.out.println("Found " + repos.size() + " repositories for accentureshark organization");
        
        // Look for quizAI repository
        boolean foundQuizAI = repos.stream()
                .anyMatch(repo -> repo.toString().contains("quizAI") || repo.toString().contains("quizai"));
        
        if (foundQuizAI) {
            System.out.println("✅ quizAI repository found in the list!");
        } else {
            System.out.println("❌ quizAI repository not found in the list");
            System.out.println("Available repositories:");
            repos.forEach(System.out::println);
        }
    }
}
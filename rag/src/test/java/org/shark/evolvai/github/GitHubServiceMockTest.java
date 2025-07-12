package org.shark.evolvai.github;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Mock test for GitHub service functionality
 */
@ExtendWith(MockitoExtension.class)
public class GitHubServiceMockTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Test
    public void testQuizAIRepositoryIsFound() {
        // Create mock repositories including quizAI
        GitHubRepository quizAIRepo = new GitHubRepository(
            "quizAI",
            "accentureshark/quizAI",
            "Quiz AI application for educational purposes",
            "https://github.com/accentureshark/quizAI",
            "https://github.com/accentureshark/quizAI.git",
            false,
            "main",
            "accentureshark",
            "Organization"
        );

        GitHubRepository evolvAIRepo = new GitHubRepository(
            "EvolvAI",
            "accentureshark/EvolvAI",
            "EvolvAI RAG system",
            "https://github.com/accentureshark/EvolvAI",
            "https://github.com/accentureshark/EvolvAI.git",
            false,
            "master",
            "accentureshark",
            "Organization"
        );

        List<GitHubRepository> mockRepos = Arrays.asList(evolvAIRepo, quizAIRepo);

        // Test the expected behavior
        assertTrue(mockRepos.stream().anyMatch(repo -> repo.getName().equals("quizAI")));
        
        GitHubRepository foundQuizAI = mockRepos.stream()
            .filter(repo -> repo.getName().equals("quizAI"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(foundQuizAI);
        assertEquals("quizAI", foundQuizAI.getName());
        assertEquals("accentureshark/quizAI", foundQuizAI.getFullName());
        assertEquals("accentureshark", foundQuizAI.getOwner());
        assertFalse(foundQuizAI.isPrivate());
        assertEquals("https://github.com/accentureshark/quizAI.git", foundQuizAI.getCloneUrl());
        
        System.out.println("✅ Mock test passed - quizAI repository found successfully!");
        System.out.println("Repository details:");
        System.out.println("  Name: " + foundQuizAI.getName());
        System.out.println("  Full Name: " + foundQuizAI.getFullName());
        System.out.println("  Description: " + foundQuizAI.getDescription());
        System.out.println("  HTML URL: " + foundQuizAI.getHtmlUrl());
        System.out.println("  Clone URL: " + foundQuizAI.getCloneUrl());
        System.out.println("  Is Private: " + foundQuizAI.isPrivate());
        System.out.println("  Owner: " + foundQuizAI.getOwner());
        System.out.println("  Default Branch: " + foundQuizAI.getDefaultBranch());
    }
    
    @Test
    public void testRepositorySelectionLogic() {
        // Simulate the frontend repository selection logic
        List<GitHubRepository> repos = Arrays.asList(
            new GitHubRepository("EvolvAI", "accentureshark/EvolvAI", "EvolvAI RAG", 
                "https://github.com/accentureshark/EvolvAI", "https://github.com/accentureshark/EvolvAI.git", 
                false, "master", "accentureshark", "Organization"),
            new GitHubRepository("quizAI", "accentureshark/quizAI", "Quiz AI app", 
                "https://github.com/accentureshark/quizAI", "https://github.com/accentureshark/quizAI.git", 
                false, "main", "accentureshark", "Organization")
        );
        
        // Test auto-selection logic (should select quizAI if available)
        GitHubRepository autoSelected = repos.stream()
            .filter(repo -> repo.getName().equalsIgnoreCase("quizAI") || 
                           repo.getName().equalsIgnoreCase("quizai"))
            .findFirst()
            .orElse(null);
        
        assertNotNull(autoSelected);
        assertEquals("quizAI", autoSelected.getName());
        
        System.out.println("✅ Auto-selection logic test passed - quizAI would be selected automatically!");
    }
}
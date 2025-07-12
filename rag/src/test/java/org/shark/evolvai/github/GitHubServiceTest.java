package org.shark.evolvai.github;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Unit test for GitHubService
 */
public class GitHubServiceTest {

    @Test
    public void testGetOrganizationRepositories() {
        GitHubService service = new GitHubService();
        
        try {
            // Test the actual GitHub API call
            Mono<List<GitHubRepository>> result = service.getOrganizationRepositories("accentureshark");
            
            List<GitHubRepository> repos = result.block();
            
            System.out.println("✅ GitHub API call successful!");
            System.out.println("Found " + repos.size() + " repositories for accentureshark organization");
            
            // Look for quizAI repository
            boolean foundQuizAI = repos.stream()
                    .anyMatch(repo -> repo.getName().equalsIgnoreCase("quizAI") || 
                                      repo.getName().equalsIgnoreCase("quizai"));
            
            if (foundQuizAI) {
                System.out.println("✅ quizAI repository found in the list!");
                
                // Print the quizAI repository details
                repos.stream()
                    .filter(repo -> repo.getName().equalsIgnoreCase("quizAI") || 
                                    repo.getName().equalsIgnoreCase("quizai"))
                    .forEach(repo -> {
                        System.out.println("Repository: " + repo.getName());
                        System.out.println("Full Name: " + repo.getFullName());
                        System.out.println("Description: " + repo.getDescription());
                        System.out.println("HTML URL: " + repo.getHtmlUrl());
                        System.out.println("Clone URL: " + repo.getCloneUrl());
                        System.out.println("Is Private: " + repo.isPrivate());
                        System.out.println("Owner: " + repo.getOwner());
                    });
            } else {
                System.out.println("❌ quizAI repository not found in the list");
                System.out.println("Available repositories:");
                repos.forEach(repo -> System.out.println("- " + repo.getName() + " (" + repo.getFullName() + ")"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error testing GitHub API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
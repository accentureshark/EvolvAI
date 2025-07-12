package org.shark.evolvai.github;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service to interact with GitHub API
 */
@Service
public class GitHubService {
    
    private final WebClient webClient;
    
    public GitHubService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.github.com")
                .build();
    }
    
    /**
     * Get public repositories for an organization
     * @param orgName organization name
     * @return list of repositories
     */
    public Mono<List<GitHubRepository>> getOrganizationRepositories(String orgName) {
        return webClient.get()
                .uri("/orgs/{orgName}/repos?type=public&per_page=100", orgName)
                .retrieve()
                .bodyToFlux(GitHubApiResponse.class)
                .map(this::mapToGitHubRepository)
                .collectList();
    }
    
    /**
     * Get all public repositories for an organization (with pagination)
     * @param orgName organization name
     * @return flux of repositories
     */
    public Flux<GitHubRepository> getAllOrganizationRepositories(String orgName) {
        return getRepositoriesWithPagination(orgName, 1);
    }
    
    private Flux<GitHubRepository> getRepositoriesWithPagination(String orgName, int page) {
        return webClient.get()
                .uri("/orgs/{orgName}/repos?type=public&per_page=100&page={page}", orgName, page)
                .retrieve()
                .bodyToFlux(GitHubApiResponse.class)
                .map(this::mapToGitHubRepository)
                .expand(repo -> {
                    // Simple pagination - if we get 100 repos, try next page
                    return getRepositoriesWithPagination(orgName, page + 1)
                            .take(100);
                });
    }
    
    private GitHubRepository mapToGitHubRepository(GitHubApiResponse response) {
        return new GitHubRepository(
                response.getName(),
                response.getFullName(),
                response.getDescription(),
                response.getHtmlUrl(),
                response.getCloneUrl(),
                response.isPrivate(),
                response.getDefaultBranch(),
                response.getOwner() != null ? response.getOwner().getLogin() : null,
                response.getOwner() != null ? response.getOwner().getType() : null
        );
    }
    
    /**
     * Inner class to map GitHub API response
     */
    @SuppressWarnings("unused")
    private static class GitHubApiResponse {
        private String name;
        private String fullName;
        private String description;
        private String htmlUrl;
        private String cloneUrl;
        private boolean isPrivate;
        private String defaultBranch;
        private Owner owner;
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getHtmlUrl() { return htmlUrl; }
        public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }
        
        public String getCloneUrl() { return cloneUrl; }
        public void setCloneUrl(String cloneUrl) { this.cloneUrl = cloneUrl; }
        
        public boolean isPrivate() { return isPrivate; }
        public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }
        
        public String getDefaultBranch() { return defaultBranch; }
        public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }
        
        public Owner getOwner() { return owner; }
        public void setOwner(Owner owner) { this.owner = owner; }
        
        private static class Owner {
            private String login;
            private String type;
            
            public String getLogin() { return login; }
            public void setLogin(String login) { this.login = login; }
            
            public String getType() { return type; }
            public void setType(String type) { this.type = type; }
        }
    }
}
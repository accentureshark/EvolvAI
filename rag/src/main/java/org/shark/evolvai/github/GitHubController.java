package org.shark.evolvai.github;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST controller for GitHub repository operations
 */
@RestController
@RequestMapping("/api/github")
public class GitHubController {
    
    @Autowired
    private GitHubService gitHubService;
    
    /**
     * Get public repositories for an organization
     * @param orgName organization name
     * @return list of repositories
     */
    @GetMapping("/organizations/{orgName}/repositories")
    public Mono<List<GitHubRepository>> getOrganizationRepositories(@PathVariable String orgName) {
        return gitHubService.getOrganizationRepositories(orgName);
    }
}
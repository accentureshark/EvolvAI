package org.shark.evolvai.github;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a GitHub repository
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepository {
    private String name;
    private String fullName;
    private String description;
    private String htmlUrl;
    private String cloneUrl;
    private boolean isPrivate;
    private String defaultBranch;
    private String owner;
    private String ownerType;
}
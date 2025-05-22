package org.shark.evolvai.inference.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "inference")
public class InferenceProperties {
    private int maxResults;
    private double minScore;
}
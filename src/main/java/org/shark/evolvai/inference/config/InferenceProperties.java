package org.shark.evolvai.inference.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "inference")
@Validated
@Data
public class InferenceProperties {

    @Min(1)
    private int maxResults;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double minScore;
}
